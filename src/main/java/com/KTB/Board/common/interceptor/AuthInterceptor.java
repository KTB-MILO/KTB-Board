package com.KTB.Board.common.interceptor;

import com.KTB.Board.common.annotation.RequireAuth;
import com.KTB.Board.session.entity.UserSession;
import com.KTB.Board.session.repository.UserSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_COOKIE = "SESSION_ID";
    private final UserSessionRepository sessionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        if (!method.hasMethodAnnotation(RequireAuth.class)) {
            return true;
        }

        String sessionId = extractSessionId(request);
        if (sessionId == null) {
            sendUnauthorized(response);
            return false;
        }

        UserSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sendUnauthorized(response);
            return false;
        }

        request.setAttribute("userId", session.getUser().getId());
        return true;
    }

    private String extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> SESSION_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"UNAUTHORIZED_USER\"}");
    }
}
