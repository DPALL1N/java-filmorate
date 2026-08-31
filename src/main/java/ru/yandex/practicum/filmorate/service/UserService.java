package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователи ID: {} и ID: {} теперь друзья", userId, friendId);
        return UserMapper.mapToUserDto(getUserByIdEntity(userId));
    }

    public UserDto removeFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Пользователь не может удалить самого себя из друзей");
        }
        getUserById(userId);
        getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователи ID: {} и ID: {} больше не друзья", userId, friendId);
        return UserMapper.mapToUserDto(getUserByIdEntity(userId));
    }

    public List<UserDto> getFriends(Long userId) {
        getUserById(userId);
        return userStorage.getFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherId) {
        if (Objects.equals(userId, otherId)) {
            throw new ValidationException("Нельзя искать общих друзей для одного и того же пользователя");
        }

        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        validateUser(user);
        if (userStorage.getUsers().stream()
                .anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()))) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        if (userStorage.getUsers().stream()
                .anyMatch(existing -> existing.getLogin().equalsIgnoreCase(user.getLogin()))) {
            throw new ValidationException("Пользователь с таким логином уже существует");
        }
        return UserMapper.mapToUserDto(userStorage.createUser(user));
    }

    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = getUserByIdEntity(userId);
        User updatedUser = UserMapper.updateUserFields(user, request);
        validateUser(updatedUser);
        return UserMapper.mapToUserDto(userStorage.updateUser(updatedUser));
    }

    private User getUserByIdEntity(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

