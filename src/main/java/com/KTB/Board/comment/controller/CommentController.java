package com.KTB.Board.comment.controller;

import com.KTB.Board.comment.dto.CreateCommentRequest;
import com.KTB.Board.comment.dto.UpdateCommentRequest;
import com.KTB.Board.comment.service.CommentService;
import com.KTB.Board.common.annotation.RequireAuth;
import com.KTB.Board.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CreateCommentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.createComment(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @PutMapping("/comments/{commentId}")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateCommentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @DeleteMapping("/comments/{commentId}")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }
}
