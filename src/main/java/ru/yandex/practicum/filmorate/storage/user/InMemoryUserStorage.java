package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.getFriends().put(friendId, ru.yandex.practicum.filmorate.model.FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, ru.yandex.practicum.filmorate.model.FriendshipStatus.CONFIRMED);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            return List.of();
        }
        return user.getFriends().keySet().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User firstUser = users.get(userId);
        User secondUser = users.get(otherId);
        if (firstUser == null || secondUser == null) {
            return List.of();
        }

        Set<Long> first = new HashSet<>(firstUser.getFriends().keySet());
        Set<Long> second = new HashSet<>(secondUser.getFriends().keySet());
        return first.stream()
                .filter(second::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
