package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("genreDbStorage")
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Genre> findByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        String query = "SELECT * FROM genres WHERE genre_id IN (" + placeholders + ") ORDER BY genre_id";

        return jdbc.query(query, mapper, ids.toArray());
    }
}
