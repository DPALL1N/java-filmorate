package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователь ID: {} поставил лайк фильму ID: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        filmDbStorage.deleteLike(filmId, userId);
        log.info("Пользователь ID: {} удалил лайк у фильма ID: {}", userId, filmId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        if (count <= 0) {
            throw new IncorrectParameterException("Параметр count должен быть больше 0");
        }
        return filmDbStorage.getPopularFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto getFilmById(Long id) {
        Film film = filmDbStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        return filmDbStorage.getFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto addFilm(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return FilmMapper.mapToFilmDto(filmDbStorage.addFilm(film));
    }

    public FilmDto updateFilm(Long filmId, UpdateFilmRequest request) {
        Film existingFilm = filmDbStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        Film updatedFilm = FilmMapper.updateFilmFields(existingFilm, request);
        validateFilm(updatedFilm);
        validateMpa(updatedFilm.getMpa());
        validateGenres(updatedFilm.getGenres());
        return FilmMapper.mapToFilmDto(filmDbStorage.updateFilm(updatedFilm));
    }

    private void validateMpa(MPA mpa) {
        if (mpa == null || mpa.getId() == null) {
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
        mpaService.getMpaById(mpa.getId());
    }

    private void validateGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        for (Genre genre : genres) {
            if (genre == null || genre.getId() == null) {
                throw new ValidationException("Жанр должен быть указан");
            }
            genreService.findById(genre.getId());
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate()
                .isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
