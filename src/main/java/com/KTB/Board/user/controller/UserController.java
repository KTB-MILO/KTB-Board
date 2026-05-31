package com.KTB.Board.user.controller;

import com.KTB.Board.common.annotation.RequireAuth;
import com.KTB.Board.common.response.ApiResponse;
import com.KTB.Board.user.dto.*;
import com.KTB.Board.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Valid SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(ApiResponse.of("SIGNUP_SUCCESS"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.of("SUCCESS", userService.getUser(userId)));
    }

    @PatchMapping("/{userId}")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");
        userService.updateUser(userId, authenticatedUserId, request);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @PutMapping("/password")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestBody @Valid UpdatePasswordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @DeleteMapping("/{userId}")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        Long authenticatedUserId = (Long) httpRequest.getAttribute("userId");
        userService.deleteUser(userId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.of("USER_DELETED"));
    }
}
