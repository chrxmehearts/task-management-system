package com.taskmanager.controller;

import com.taskmanager.dto.request.LoginRequestDto;
import com.taskmanager.dto.request.RegisterRequestDto;
import com.taskmanager.dto.response.AuthResponseDto;
import com.taskmanager.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ui")
@RequiredArgsConstructor
@Slf4j
public class UiAuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session) {
        try {
            RegisterRequestDto request = RegisterRequestDto.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .build();
            authService.register(request);

            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username(username)
                    .password(password)
                    .build();
            AuthResponseDto authResponse = authService.login(loginRequest);

            session.setAttribute("jwt_token", authResponse.getToken());
            session.setAttribute("username", authResponse.getUsername());

            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return "redirect:/register?error=" + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        try {
            LoginRequestDto request = LoginRequestDto.builder()
                    .username(username)
                    .password(password)
                    .build();
            AuthResponseDto authResponse = authService.login(request);

            session.setAttribute("jwt_token", authResponse.getToken());
            session.setAttribute("username", authResponse.getUsername());

            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return "redirect:/login?error=Invalid credentials";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
