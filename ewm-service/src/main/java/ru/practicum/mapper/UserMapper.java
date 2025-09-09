package ru.practicum.mapper;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

public class UserMapper {
    public static User toModel(NewUserRequest dto) {
        return User.builder().name(dto.getName()).email(dto.getEmail()).build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail()).build();
    }

    public static UserShortDto toShortDto(User user) {
        return UserShortDto.builder().id(user.getId()).name(user.getName()).build();
    }
}
