package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Поле title не должно быть пустым")
    @Size(min = 1, max = 50, message = "Заголовок должен быть длиной от 1 до 50 символов")
    private String title;
}
