package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    public CommentDto createNewComment(NewCommentDto dto, Long userId, Long eventId) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        Event event = eventService.getPublishedEventOrElseThrow(eventId);
        Comment commentToSave = CommentMapper.toModel(dto, user, event);
        Comment savedComment = commentRepository.saveAndFlush(commentToSave);
        return CommentMapper.toDto(savedComment);
    }

    public CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto dto) {
        User user = userService.getUserIfExistOrElseThrow(userId);
        Comment oldComment = getCommentIfExistOrElseThrow(commentId);

        if (!oldComment.getAuthor().equals(user)) {
            String errorMessage = String.format("Пользователь с id = %d не является автором комментария", userId);
            throw new ValidationException(errorMessage);
        }

        if (oldComment.getCreated().plusMinutes(15).isBefore(LocalDateTime.now())) {
            String errorMessage = "Редактирование комментария разрешено только в течение 15 минут с момента публикации";
            throw new ValidationException(errorMessage);
        }

        oldComment.setText(dto.getText());
        Comment updatedComment = commentRepository.saveAndFlush(oldComment);

        return CommentMapper.toDto(updatedComment);
    }

    private Comment getCommentIfExistOrElseThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + id + " не найден"));
    }

    public void deleteComment(Long commentId) {
        getCommentIfExistOrElseThrow(commentId);
        commentRepository.deleteById(commentId);
        log.info("Успешно удален комментарий с id = {}", commentId);
    }

    public CommentDto getCommentById(Long commentId) {
        Comment comment = getCommentIfExistOrElseThrow(commentId);
        return CommentMapper.toDto(comment);
    }

    public List<CommentDto> getCommentsToEvent(Long eventId, int from, int size) {
        eventService.getPublishedEventOrElseThrow(eventId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable).getContent();
        return comments.stream().map(CommentMapper::toDto).toList();
    }

    public List<CommentDto> getAllUserComments(Long userId, int from, int size) {
        userService.getUserIfExistOrElseThrow(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable).getContent();
        return comments.stream().map(CommentMapper::toDto).toList();
    }
}
