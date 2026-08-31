package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Long id);

    Optional<Film> getFilmById(Long id);

    List<Film> getPopularFilms(int count);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);
}
