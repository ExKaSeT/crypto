package edu.example.springmvcdemo.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterDto {
    @NotNull(message = "Логин не может отсутствовать")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "В логине можно использовать только латинские символы и цифры")
    @Length(min = 5, max = 20, message = "Логин должен содержать от 5 до 20 символов")
    private String username;
    @NotNull(message = "Пароль не может отсутствовать")
    @Length(min = 5, max = 20, message = "Пароль должен содержать от 5 до 20 символов")
    private String password;
}
