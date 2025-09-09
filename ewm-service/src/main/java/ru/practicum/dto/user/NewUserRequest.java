package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
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
public class NewUserRequest {

    @NotBlank(message = "Поле email не должно быть пустым")
    @Email(message = "Email не соответствует формату: example@mail.ru")
    @Size(min = 6, max = 254, message = "Email должен быть длиной от 6 до 254 символов")
    private String email;

    @NotBlank(message = "Поле name не должно быть пустым")
    @Size(min = 2, max = 250, message = "Имя должно быть длиной от 2 до 250 символов")
    private String name;
}
