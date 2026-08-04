package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получен запрос на получение списка всех пользователей. Всего пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        validate(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с ID: {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление данных пользователя: {}", user);
        if (user.getId() == null) {
            log.error("Попытка обновления пользователя без указания ID");
            throw new ValidationException("ID пользователя должен быть указан для обновления");
        }
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не существует");
        }
        validate(user);
        users.put(user.getId(), user);
        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return user;
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Валидация не пройдена: некорректный email ({})", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Валидация не пройдена: некорректный логин ({})", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Валидация не пройдена: дата рождения в будущем ({})", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
