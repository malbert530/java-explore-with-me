package ru.practicum.controller.adminApi;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/admin/comments/{commentId}")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService service;

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Min(1) Long commentId) {
        log.info("Получен запрос на удаление комментария с id = {}", commentId);
        service.deleteComment(commentId);
    }

    @GetMapping
    public CommentDto getCommentById(@PathVariable @Min(1) Long commentId) {
        log.info("Получен запрос на получение комментария с id = {}", commentId);
        return service.getCommentById(commentId);
    }
}
