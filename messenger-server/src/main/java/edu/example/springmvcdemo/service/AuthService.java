package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dto.auth.AuthUserDto;
import edu.example.springmvcdemo.dto.auth.LoginRequestDto;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import edu.example.springmvcdemo.model.Role;
import edu.example.springmvcdemo.model.User;
import edu.example.springmvcdemo.security.jwt.JwtService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public AuthUserDto register(RegisterRequestDto registerRequest) throws ConstraintViolationException {
        if (userService.isUserExist(registerRequest.getUsername())) {
            throw new ConstraintViolationException("Provided username already taken", null);
        }
        var user = userService.createUser(registerRequest.getUsername(), registerRequest.getPassword(), Role.USER);
        var tokens = jwtService.buildTokens(user);

        return new AuthUserDto(tokens.getAccessToken(), tokens.getRefreshToken(), user.getUsername());
    }

    public AuthUserDto login(LoginRequestDto loginRequest) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        User user = userService.getUserByUsername(loginRequest.getUsername());
        var tokens = jwtService.buildTokens(user);

        return new AuthUserDto(tokens.getAccessToken(), tokens.getRefreshToken(), user.getUsername());
    }
}
