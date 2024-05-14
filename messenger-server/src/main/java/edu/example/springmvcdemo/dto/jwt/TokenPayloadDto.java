package edu.example.springmvcdemo.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPayloadDto {
    private String username;
    /**
     * Null in access token
     */
    private Long tokenId;

    public boolean isAccessToken() {
        return tokenId == null;
    }
}
