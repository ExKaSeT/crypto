package edu.example.springmvcdemo.security.jwt;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    public static final String JWT_ACCESS_COOKIE_NAME = "accessToken";
    public static final String JWT_REFRESH_COOKIE_NAME = "refreshToken";
    public static final String PATH_TO_UPDATE_TOKENS = "/auth/token";

    private boolean cookieHttpsOnly = true;

    @NotNull
    private String secretKey;

    @NotNull
    private Long accessTokenExpirationMin;

    @NotNull
    private Long refreshTokenExpirationMin;
}
