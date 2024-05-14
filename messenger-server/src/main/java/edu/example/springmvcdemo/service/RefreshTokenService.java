package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.RefreshTokenRepository;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.RefreshToken;
import edu.example.springmvcdemo.model.User;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import static edu.example.springmvcdemo.security.jwt.JwtProperties.JWT_REFRESH_COOKIE_NAME;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;

    public RefreshToken saveToken(User user, Instant validUntil) {
        RefreshToken token = new RefreshToken();
        token.setWasUsed(false);
        token.setUser(user);
        token.setValidUntilUtc0(LocalDateTime.ofInstant(validUntil, ZoneId.of("Z")));
        return tokenRepository.save(token);
    }

    public RefreshToken getToken(Long id) {
        return tokenRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Token not found"));
    }

    public void setUsed(RefreshToken token) {
        token.setWasUsed(true);
        tokenRepository.save(token);
    }
    @Transactional
    public void deactivateUserTokens(String username) {
        tokenRepository.deleteAllByUserUsername(username);
    }

    /**
     * Does not validate the token
     */
    @Nullable
    public String extractFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> tokenCookieOpt = Optional.empty();
        if (nonNull(cookies)) {
            tokenCookieOpt = Arrays.stream(cookies)
                    .filter(c -> JWT_REFRESH_COOKIE_NAME.equals(c.getName()))
                    .findFirst();
        }

        return tokenCookieOpt.map(Cookie::getValue).orElse(null);
    }

    @Transactional
    public void deleteExpiredRefreshTokens() {
        var currentDateUtc0 = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Z"));
        tokenRepository.deleteAllByValidUntilUtc0IsLessThan(currentDateUtc0);
    }
}
