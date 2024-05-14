package edu.example.springmvcdemo.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @Length(min = 5, max = 25)
    private String username;

    @NotEmpty
    private String password;
}
