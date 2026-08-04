package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    private long nextId = 1;

    private static final LocalDate START_FILMS_ERA = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получен запрос на получение списка всех фильмов. Всего фильмов: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        validate(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен под ID: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        if (film.getId() == null) {
            log.error("Попытка обновления фильма без указания ID");
            throw new ValidationException("ID фильма должен быть указан для обновления");
        }
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не существует");
        }

        validate(film);

        films.put(film.getId(), film);
        log.info("Фильм с ID {} успешно обновлен", film.getId());
        return film;
    }

    private void validate(Film film) {
        if (film.getReleaseDate() != null || film.getReleaseDate().isBefore(START_FILMS_ERA)) {
            log.error("Валидация не пройдена: дата релиза раньше 28.12.1895 ({})", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
    }

}
