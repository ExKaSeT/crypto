package edu.example.springmvcdemo.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensDto {
    private String accessToken;
    private String refreshToken;
}
