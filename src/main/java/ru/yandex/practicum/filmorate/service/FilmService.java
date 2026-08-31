package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       MpaService mpaService,
                       GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь ID: {} поставил лайк фильму ID: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь ID: {} удалил лайк у фильма ID: {}", userId, filmId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        if (count <= 0) {
            throw new IncorrectParameterException("Параметр count должен быть больше 0");
        }
        return filmStorage.getPopularFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto getFilmById(Long id) {
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto addFilm(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());
        return FilmMapper.mapToFilmDto(filmStorage.addFilm(film));
    }

    public FilmDto updateFilm(Long filmId, UpdateFilmRequest request) {
        Film existingFilm = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        Film updatedFilm = FilmMapper.updateFilmFields(existingFilm, request);
        validateFilm(updatedFilm);
        validateMpa(updatedFilm.getMpa());
        validateGenres(updatedFilm.getGenres());
        return FilmMapper.mapToFilmDto(filmStorage.updateFilm(updatedFilm));
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

        Set<Integer> genreIds = genres.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (genreIds.isEmpty()) {
            throw new ValidationException("Жанр должен быть указан");
        }

        List<Genre> foundGenres = genreService.findByIds(genreIds);
        Set<Integer> foundIds = foundGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        for (Integer genreId : genreIds) {
            if (!foundIds.contains(genreId)) {
                throw new NotFoundException("Жанр с ID " + genreId + " не найден");
            }
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
