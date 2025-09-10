package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.EventService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createNewEvent(@RequestBody @Valid NewEventDto dto, @PathVariable @Min(1) Long userId) {
        log.info("Получен запрос на создание нового события {} от пользователя с id = {}", dto, userId);
        return service.createNewEvent(dto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEventById(@PathVariable @Min(1) Long userId, @PathVariable @Min(1) Long eventId) {
        log.info("Получен запрос на получение события с id = {} от пользователя с id = {}", eventId, userId);
        return service.getUserEventById(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getAllUserEvents(@RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") @Min(1) int size,
                                                @PathVariable @Min(1) Long userId) {
        log.info("Получен запрос на получение событий пользователя с id = {}", userId);
        return service.getAllUserEvents(from, size, userId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@RequestBody @Valid UpdateEventUserRequest dto, @PathVariable @Min(1) Long userId,
                                          @PathVariable @Min(1) Long eventId) {
        log.info("Получен запрос на обновление события с id = {} от пользователя с id = {}", eventId, userId);
        return service.updateEventByUser(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getUserEventRequests(@PathVariable @Min(1) Long userId, @PathVariable @Min(1) Long eventId) {
        log.info("Получен запрос на получение заявок на событие с id = {} пользователя с id = {}", eventId, userId);
        return service.getUserEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateUserEventRequests(@PathVariable @Min(1) Long userId, @PathVariable @Min(1) Long eventId,
                                                @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("Получен запрос на изменение статуса заявок на событие с id = {} пользователя с id = {}", eventId, userId);
        return service.updateUserEventRequests(userId, eventId, request);
    }
}
