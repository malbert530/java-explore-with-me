package ru.practicum.controller.adminApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createNewUser(@Valid @RequestBody NewUserRequest dto) {
        log.info("Получен запрос на создание нового пользователя {}", dto);
        return service.createNewUser(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Min(1) Long id) {
        log.info("Получен запрос на удаление пользователя с id = {}", id);
        service.deleteUser(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "10") @Min(1) int size,
                                     @RequestParam(required = false) List<Long> ids) {
        log.info("Получен запрос на получение всех категорий с id = {}, from = {}, size = {}", ids, from, size);
        return service.getAllUsers(from, size, ids);
    }
}
