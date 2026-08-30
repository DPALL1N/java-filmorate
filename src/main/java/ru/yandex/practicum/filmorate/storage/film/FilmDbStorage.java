package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT f.*, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
            "ORDER BY f.film_id";
    private static final String FIND_BY_ID_QUERY = "SELECT f.*, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
            "WHERE f.film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY =
            "SELECT g.* " +
            "FROM genres g " +
            "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
            "WHERE fg.film_id = ? " +
            "ORDER BY g.genre_id";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_BY_FILM_QUERY =
            "SELECT user_id " +
            "FROM film_likes WHERE film_id = ? ORDER BY user_id";
    private static final String GET_POPULAR_QUERY =
            "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
            "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
            "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
            "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
            "ORDER BY likes_count DESC, f.film_id ASC LIMIT ?";

    private final SimpleJdbcInsert jdbcInsert;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
        this.jdbcInsert = new SimpleJdbcInsert(jdbc.getDataSource())
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
    }

    @Override
    public Collection<Film> getFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::loadGenres);
        films.forEach(this::loadLikes);
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("duration", film.getDuration());

        if (film.getDescription() != null) {
            params.put("description", film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            params.put("release_date", Date.valueOf(film.getReleaseDate()));
        }
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            params.put("mpa_id", film.getMpa().getId());
        }

        long filmId = jdbcInsert.executeAndReturnKey(params).longValue();
        film.setId(filmId);
        saveGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        delete(DELETE_QUERY, id);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, id);
        film.ifPresent(f -> {
            loadGenres(f);
            loadLikes(f);
        });
        return film;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = jdbc.query(GET_POPULAR_QUERY, mapper, count);
        films.forEach(this::loadGenres);
        films.forEach(this::loadLikes);
        return films;
    }

    public void addLike(Long filmId, Long userId) {
        jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    private void saveGenres(Film film) {
        jdbc.update(DELETE_GENRES_QUERY, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(INSERT_GENRE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setInt(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadLikes(Film film) {
        List<Long> likes = jdbc.queryForList(FIND_LIKES_BY_FILM_QUERY, Long.class, film.getId());
        film.setLikes(new LinkedHashSet<>(likes));
    }
}
