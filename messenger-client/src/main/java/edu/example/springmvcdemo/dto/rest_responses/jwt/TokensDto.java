package edu.example.springmvcdemo.dto.rest_responses.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensDto {
    private String accessToken;
    private String refreshToken;
}
