package com.taskmanager.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        String token = (String) session.getAttribute("jwt_token");
        if (token == null) {
            response.sendRedirect("/login");
            return false;
        }

        try {
            jwtTokenProvider.extractUsername(token);
        } catch (Exception e) {
            log.warn("Invalid session token, redirecting to login: {}", e.getMessage());
            session.invalidate();
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
