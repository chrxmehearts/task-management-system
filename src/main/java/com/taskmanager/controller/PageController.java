package com.taskmanager.controller;

import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtTokenProvider;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PageController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String indexPage(HttpSession session) {
        if (hasValidSession(session)) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (hasValidSession(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (hasValidSession(session)) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    private boolean hasValidSession(HttpSession session) {
        String token = (String) session.getAttribute("jwt_token");
        if (token == null) return false;
        if (!jwtTokenProvider.isTokenNotExpired(token)) {
            session.removeAttribute("jwt_token");
            return false;
        }
        try {
            String username = jwtTokenProvider.extractUsername(token);
            boolean exists = userRepository.findByUsername(username).isPresent();
            if (!exists) {
                session.removeAttribute("jwt_token");
            }
            return exists;
        } catch (Exception e) {
            log.error("Session validation error: {}", e.getMessage());
            session.removeAttribute("jwt_token");
            return false;
        }
    }
}
