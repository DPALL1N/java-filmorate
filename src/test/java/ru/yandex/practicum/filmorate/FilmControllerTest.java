package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, FilmRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerTest {
    private final FilmDbStorage filmStorage;

    @Test
    void shouldCreateAndFindFilm() {
        Film film = new Film();
        film.setName("Test film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        MPA mpa = new MPA();
        mpa.setId(1);
        film.setMpa(mpa);

        Film saved = filmStorage.addFilm(film);

        assertThat(saved.getId()).isNotNull();
        Optional<Film> found = filmStorage.getFilmById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test film");
    }
}
