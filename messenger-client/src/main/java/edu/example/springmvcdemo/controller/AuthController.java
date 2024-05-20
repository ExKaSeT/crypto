package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.auth.RegisterDto;
import edu.example.springmvcdemo.service.UserSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserSessionService userSessionService;

    @GetMapping("/")
    public String get() {
        return "home";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerDto") @Valid RegisterDto registerDto, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userSessionService.loginRegister(registerDto.getUsername(), registerDto.getPassword(), true);
        } catch (Exception ex) {
            result.addError(new ObjectError("errors", ex.getMessage()));
            return "register";
        }

        return "redirect:/login";
    }
}
