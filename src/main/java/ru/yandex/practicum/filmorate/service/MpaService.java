package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public List<MPA> getAllMpa() {
        return mpaDbStorage.getAllMpa();
    }

    public MPA getMpaById(int id) {
        return mpaDbStorage.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + id + " не найден"));
    }
}
