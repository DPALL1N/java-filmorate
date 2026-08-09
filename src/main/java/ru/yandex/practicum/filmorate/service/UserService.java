package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addFriend(Long userId, Long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (friend.getFriends().containsKey(userId)) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
            log.info("Пользователи ID: {} и ID: {} теперь взаимные друзья (CONFIRMED)", userId, friendId);
        } else {
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
            log.info("Пользователь ID: {} отправил заявку в друзья пользователю ID: {} (UNCONFIRMED)", userId, friendId);
        }

        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователи ID: {} и ID: {} теперь друзья", userId, friendId);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new ValidationException("Пользователь не может удалить самого себя из друзей");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователи ID: {} и ID: {} больше не друзья", userId, friendId);
        return user;
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        return user.getFriends().keySet().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (Objects.equals(userId, otherId)) {
            throw new ValidationException("Нельзя искать общих друзей для одного и того же пользователя");
        }

        User user = getUserById(userId);
        User otherUser = getUserById(otherId);

        Set<Long> commonFriendsIds = new HashSet<>(user.getFriends().keySet());
        Set<Long> otherFriends = otherUser.getFriends().keySet();
        commonFriendsIds.retainAll(otherFriends);

        return commonFriendsIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        getUserById(user.getId());
        return userStorage.updateUser(user);
    }
}
