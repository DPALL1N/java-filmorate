package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private final UserDbStorage userStorage;

    @Test
    void shouldCreateAndFindUser() {
        User user = new User();
        user.setEmail("new-user@test.ru");
        user.setLogin("new_login");
        user.setName("New User");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User saved = userStorage.createUser(user);

        assertThat(saved.getId()).isNotNull();
        Optional<User> found = userStorage.getUserById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("new-user@test.ru");
    }

    @Test
    void shouldFindCreatedUserById() {
        User user = new User();
        user.setEmail("find-user@test.ru");
        user.setLogin("find_user");
        user.setName("Find User");
        user.setBirthday(LocalDate.of(2001, 2, 3));

        User saved = userStorage.createUser(user);
        Optional<User> found = userStorage.getUserById(saved.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(value -> assertThat(value.getId()).isEqualTo(saved.getId()));
    }
}
