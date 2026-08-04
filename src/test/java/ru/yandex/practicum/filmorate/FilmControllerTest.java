package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void shouldAddFilmWithValidData() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film addedFilm = filmController.createFilm(film);

        assertNotNull(addedFilm.getId());
        assertEquals("Фильм", addedFilm.getName());
    }

    @Test
    void shouldThrowExceptionWhenFilmNameIsEmpty() {
        Film film = new Film();
        film.setName(" "); // Пустое имя
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeCinemaBirthday() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film));
    }
}