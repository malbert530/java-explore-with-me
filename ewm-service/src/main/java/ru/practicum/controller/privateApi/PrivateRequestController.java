package ru.practicum.controller.privateApi;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Min(1) Long userId, @RequestParam(required = true) Long eventId) {
        log.info("Получен запрос на создание заявки на участие в событии с id = {} от пользователя с id = {}", eventId, userId);
        return service.createNewRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Min(1) Long userId) {
        log.info("Получен запрос на получение всех заявок на события от пользователя с id = {}", userId);
        return service.getUserRequests(userId);
    }

    @PatchMapping("{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Min(1) Long userId, @PathVariable @Min(1) Long requestId) {
        log.info("Получен запрос на отмену заявки с id = {} от пользователя с id = {}", requestId, userId);
        return service.cancelRequest(userId, requestId);
    }
}
