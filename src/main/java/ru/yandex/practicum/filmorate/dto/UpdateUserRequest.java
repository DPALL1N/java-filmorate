package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private Long id;
    private String name;
    private String email;
    private String login;
    private LocalDate birthday;
}
