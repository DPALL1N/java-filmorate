package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("mpaDbStorage")
public class MpaDbStorage extends BaseRepository<MPA> {
    private static final String GET_ALL_QUERY = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<MPA> mapper) {
        super(jdbc, mapper);
    }

    public List<MPA> getAllMpa() {
        return findMany(GET_ALL_QUERY);
    }

    public Optional<MPA> getMpaById(int mpaId) {
        return findOne(FIND_BY_ID_QUERY, mpaId);
    }
}
