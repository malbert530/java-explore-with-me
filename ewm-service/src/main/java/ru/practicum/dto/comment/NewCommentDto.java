package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "Комментарий не должен быть пустой")
    @Size(min = 1, max = 500, message = "Комментарий должен содержать от 1 до 500 символов")
    private String text;
}
