package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.config.RestClientConfig;
import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.dao.RoomRepository;
import edu.example.springmvcdemo.dao.UserSessionRepository;
import edu.example.springmvcdemo.dto.rest_responses.auth.LoginRequestDto;
import edu.example.springmvcdemo.model.UserSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    public static final String ACCESS_COOKIE_NAME = "accessToken";
    public static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final UserSessionRepository userSessionRepository;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final RestClientConfig restClientConfig;

    @Getter
    private String accessToken = null;

    public void clearSession() {
        userSessionRepository.deleteAll();
        messageRepository.deleteAll();
        roomRepository.deleteAll();
    }

    public UserSession createUpdateSession(String accessToken, String refreshToken, String username) {
        var currentUser = userSessionRepository.findAll();
        UserSession user;
        if (!currentUser.isEmpty()) {
            if (currentUser.get(0).getUsername().equals(username)) {
                user = currentUser.get(0);
            } else {
                clearSession();
                user = new UserSession();
            }
        } else {
            user = new UserSession();
        }
        user.setUsername(username);
        user.setRefreshToken(refreshToken);
        user = userSessionRepository.save(user);

        this.accessToken = accessToken;
        return user;
    }

    public UserSession loginRegister(String username, String password, boolean isRegister) throws AuthenticationException {
        var uri = isRegister ? restClientConfig.getUri("/auth/register") :
                restClientConfig.getUri("/auth/login");

        AtomicReference<String> accessToken = new AtomicReference<>();
        AtomicReference<String> refreshToken = new AtomicReference<>();

        restClientConfig.getRestClient().post()
                .uri(uri)
                .body(new LoginRequestDto(username, password))
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new AuthenticationServiceException("Status: " + response.getStatusCode() + ". Message: "
                                + response.getStatusText());
                    }

                    try {
                        var cookieMap = UserSessionService.parseCookie(requireNonNull(response.getHeaders().get("Set-Cookie")));
                        accessToken.set(cookieMap.get(ACCESS_COOKIE_NAME));
                        refreshToken.set(cookieMap.get(REFRESH_COOKIE_NAME));
                        requireNonNull(accessToken.get());
                        requireNonNull(refreshToken.get());
                    } catch (Exception ex) {
                        throw new AuthenticationServiceException("Can't get cookie from response: " + ex.getMessage());
                    }
                    return null;
                });

        return createUpdateSession(accessToken.get(), refreshToken.get(), username);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateTokens() {
        var userList = userSessionRepository.findAll();
        if (userList.isEmpty()) {
            return;
        }
        var user = userList.get(0);

        AtomicReference<String> accessToken = new AtomicReference<>();
        AtomicReference<String> refreshToken = new AtomicReference<>();

        restClientConfig.getRestClient().post()
                .uri(restClientConfig.getUri("/auth/token/update"))
                .header("Cookie", getCookieString(Map.of(REFRESH_COOKIE_NAME, user.getRefreshToken())))
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new AuthenticationServiceException(response.getStatusText());
                    }

                    try {
                        var cookieMap = parseCookie(requireNonNull(response.getHeaders().get("Set-Cookie")));
                        accessToken.set(cookieMap.get(ACCESS_COOKIE_NAME));
                        refreshToken.set(cookieMap.get(REFRESH_COOKIE_NAME));
                        requireNonNull(accessToken.get());
                        requireNonNull(refreshToken.get());
                    } catch (Exception ex) {
                        throw new AuthenticationServiceException("Can't get cookie from response: " + ex.getMessage());
                    }
                    return null;
                });

        createUpdateSession(accessToken.get(), refreshToken.get(), user.getUsername());
    }

    public static Map<String, String> parseCookie(List<String> cookieStrings) {
        return cookieStrings.stream()
                .map(cookie -> cookie.split(";")[0])
                .collect(Collectors.toMap(val -> val.split("=")[0], val -> val.split("=")[1]));
    }

    public static String getCookieString(Map<String, String> cookies) {
        return cookies.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("; "));
    }
}
