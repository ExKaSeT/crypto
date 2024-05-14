package edu.example.springmvcdemo.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {
    @JsonIgnore
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private String username;
}
