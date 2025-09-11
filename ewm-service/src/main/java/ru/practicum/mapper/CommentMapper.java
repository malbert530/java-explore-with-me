package ru.practicum.mapper;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static Comment toModel(NewCommentDto dto, User author, Event event) {
        return Comment.builder()
                .text(dto.getText())
                .created(LocalDateTime.now())
                .author(author)
                .event(event)
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(UserMapper.toShortDto(comment.getAuthor()))
                .eventId(comment.getEvent().getId())
                .created(comment.getCreated())
                .text(comment.getText())
                .build();
    }
}
