package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.auth.AuthUserDto;
import edu.example.springmvcdemo.dto.auth.LoginRequestDto;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import edu.example.springmvcdemo.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenController tokenController;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDto requestDto, HttpServletResponse response) {
        AuthUserDto authUser = authService.register(requestDto);
        tokenController.addTokenCookiesToResponse(response, authUser.getAccessToken(), authUser.getRefreshToken());
        return ResponseEntity.ok(authUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        AuthUserDto authUser = authService.login(requestDto);
        tokenController.addTokenCookiesToResponse(response, authUser.getAccessToken(), authUser.getRefreshToken());
        return ResponseEntity.ok(authUser);
    }
}