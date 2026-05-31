package com.KTB.Board.auth.controller;

import com.KTB.Board.auth.dto.LoginRequest;
import com.KTB.Board.auth.service.AuthService;
import com.KTB.Board.common.annotation.RequireAuth;
import com.KTB.Board.common.interceptor.AuthInterceptor;
import com.KTB.Board.common.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        String sessionId = authService.login(request.getEmail(), request.getPassword());
        Cookie cookie = new Cookie(AuthInterceptor.SESSION_COOKIE, sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.of("LOGIN_SUCCESS"));
    }

    @DeleteMapping
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        String sessionId = Arrays.stream(request.getCookies())
                .filter(c -> AuthInterceptor.SESSION_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        if (sessionId != null) {
            authService.logout(sessionId);
        }
        Cookie expired = new Cookie(AuthInterceptor.SESSION_COOKIE, null);
        expired.setMaxAge(0);
        expired.setPath("/");
        response.addCookie(expired);
        return ResponseEntity.ok(ApiResponse.of("SIGNOUT_SUCCESS"));
    }
}
