package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers() {
        log.info("Получен запрос на получение списка всех пользователей");
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Получен запрос на получение пользователя с ID: {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest userRequest) {
        log.info("Получен запрос на создание пользователя: {}", userRequest);
        return userService.createUser(userRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Получен запрос на обновление данных пользователя: {}", request);
        if (request.getEmail() == null && request.getLogin() == null && request.getName() == null && request.getBirthday() == null) {
            throw new ValidationException("Необходимо передать хотя бы одно поле для обновления");
        }
        return userService.updateUser(request.getId(), request);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос: пользователь ID {} добавляет в друзья ID {}", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрос: пользователь ID {} удаляет из друзей ID {}", id, friendId);
        return userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getFriends(@PathVariable Long id) {
        log.info("Запрос на получение списка друзей у пользователя ID {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрос на получение общих друзей пользователя ID {} и ID {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
