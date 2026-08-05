package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь ID: {} поставил лайк фильму ID: {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь ID: {} удалил лайк у фильма ID: {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new IncorrectParameterException("Параметр count должен быть больше 0");
        }
        return filmStorage.getPopularFilms(count);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        return filmStorage.updateFilm(film);
    }
}