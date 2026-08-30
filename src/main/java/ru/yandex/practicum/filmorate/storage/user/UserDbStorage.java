package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users ORDER BY user_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_BY_LOGIN_QUERY = "SELECT * FROM users WHERE login = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendships(user_id, friend_id, status) " +
            "VALUES (?, ?, false)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_QUERY = "SELECT u.* FROM users u " +
            "JOIN friendships f ON u.user_id = f.friend_id WHERE f.user_id = ? ORDER BY u.user_id";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User createUser(User user) {
        long id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        delete(DELETE_QUERY, id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public Optional<User> findByLogin(String login) {
        return findOne(FIND_BY_LOGIN_QUERY, login);
    }

    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        return findMany(GET_FRIENDS_QUERY, userId);
    }
}
