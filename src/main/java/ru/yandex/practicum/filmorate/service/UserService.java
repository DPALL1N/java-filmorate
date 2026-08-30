package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDbStorage userDbStorage;

    public UserDto addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }
        getUserById(userId);
        getUserById(friendId);
        userDbStorage.addFriend(userId, friendId);
        log.info("Пользователи ID: {} и ID: {} теперь друзья", userId, friendId);
        return UserMapper.mapToUserDto(getUserByIdEntity(userId));
    }

    public UserDto removeFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Пользователь не может удалить самого себя из друзей");
        }
        getUserById(userId);
        getUserById(friendId);
        userDbStorage.removeFriend(userId, friendId);
        log.info("Пользователи ID: {} и ID: {} больше не друзья", userId, friendId);
        return UserMapper.mapToUserDto(getUserByIdEntity(userId));
    }

    public List<UserDto> getFriends(Long userId) {
        getUserById(userId);
        return userDbStorage.getFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherId) {
        if (Objects.equals(userId, otherId)) {
            throw new ValidationException("Нельзя искать общих друзей для одного и того же пользователя");
        }

        List<User> userFriends = userDbStorage.getFriends(userId);
        List<User> otherFriends = userDbStorage.getFriends(otherId);
        Set<Long> otherIds = new HashSet<>();
        for (User user : otherFriends) {
            otherIds.add(user.getId());
        }

        List<UserDto> common = new ArrayList<>();
        for (User user : userFriends) {
            if (otherIds.contains(user.getId())) {
                common.add(UserMapper.mapToUserDto(user));
            }
        }
        return common;
    }

    public UserDto getUserById(Long id) {
        User user = userDbStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> getUsers() {
        return userDbStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        validateUser(user);
        if (userDbStorage.findByEmail(user.getEmail()).isPresent()) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        if (userDbStorage.findByLogin(user.getLogin()).isPresent()) {
            throw new ValidationException("Пользователь с таким логином уже существует");
        }
        return UserMapper.mapToUserDto(userDbStorage.createUser(user));
    }

    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = getUserByIdEntity(userId);
        User updatedUser = UserMapper.updateUserFields(user, request);
        validateUser(updatedUser);
        return UserMapper.mapToUserDto(userDbStorage.updateUser(updatedUser));
    }

    private User getUserByIdEntity(Long id) {
        return userDbStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

