package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto getFilmById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с ID: {}", id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto addFilm(@Valid @RequestBody NewFilmRequest filmRequest) {
        log.info("Получен запрос на добавление фильма: {}", filmRequest);
        return filmService.addFilm(filmRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public FilmDto updateFilm(@Valid @RequestBody UpdateFilmRequest filmRequest) {
        log.info("Получен запрос на обновление фильма: {}", filmRequest);
        if (filmRequest.getId() == null) {
            throw new ValidationException("ID фильма должен быть указан для обновления");
        }
        return filmService.updateFilm(filmRequest.getId(), filmRequest);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос: пользователь ID {} ставит лайк фильму ID {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Запрос: пользователь ID {} удаляет лайк у фильма ID {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос на получение первых {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }
}
