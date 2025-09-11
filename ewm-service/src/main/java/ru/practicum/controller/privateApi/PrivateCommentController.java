package ru.practicum.controller.privateApi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createNewEvent(@RequestBody @Valid NewCommentDto dto, @PathVariable @Min(1) Long userId,
                                     @RequestParam @Min(1) Long eventId) {
        log.info("Получен запрос на создание нового комментария на событие с id = {} от пользователя с id = {}", eventId, userId);
        return service.createNewComment(dto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentByUser(@RequestBody @Valid NewCommentDto dto, @PathVariable @Min(1) Long userId,
                                          @PathVariable @Min(1) Long commentId) {
        log.info("Получен запрос на обновление комментария с id = {} от пользователя с id = {}", commentId, userId);
        return service.updateCommentByUser(userId, commentId, dto);
    }

    @GetMapping
    public List<CommentDto> getAllUserComments(@PathVariable @Min(1) Long userId, @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос на получение комментариев к событию с id = {}", userId);
        return service.getAllUserComments(userId, from, size);
    }
}
