package ru.practicum.controller.publicApi;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/comments/{eventId}")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService service;

    @GetMapping
    public List<CommentDto> getCommentsToEvent(@PathVariable @Min(1) Long eventId, @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос на получение комментариев к событию с id = {}", eventId);
        return service.getCommentsToEvent(eventId, from, size);
    }
}
