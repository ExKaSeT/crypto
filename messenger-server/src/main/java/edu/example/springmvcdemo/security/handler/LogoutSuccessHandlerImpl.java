package edu.example.springmvcdemo.security.handler;

import edu.example.springmvcdemo.security.exception.InvalidTokenException;
import edu.example.springmvcdemo.security.jwt.JwtService;
import edu.example.springmvcdemo.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import static edu.example.springmvcdemo.security.jwt.JwtProperties.JWT_ACCESS_COOKIE_NAME;
import static edu.example.springmvcdemo.security.jwt.JwtProperties.JWT_REFRESH_COOKIE_NAME;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final RefreshTokenService tokenService;
    private final JwtService jwtService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = tokenService.extractFromRequest(request);

        if (nonNull(refreshToken)) {
            var payload = jwtService.parseToken(refreshToken);
            if (payload.isAccessToken()) {
                throw new InvalidTokenException("Invalid token");
            }
            var token = tokenService.getToken(payload.getTokenId());
            tokenService.setUsed(token);
        }

        CookieClearingLogoutHandler clearingLogoutHandler = new CookieClearingLogoutHandler(JWT_REFRESH_COOKIE_NAME, JWT_ACCESS_COOKIE_NAME);
        clearingLogoutHandler.logout(request, response, authentication);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
