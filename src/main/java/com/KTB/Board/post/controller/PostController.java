package com.KTB.Board.post.controller;

import com.KTB.Board.common.annotation.RequireAuth;
import com.KTB.Board.common.response.ApiResponse;
import com.KTB.Board.common.response.PageResponse;
import com.KTB.Board.post.dto.*;
import com.KTB.Board.post.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListResponse>>> getPosts(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(ApiResponse.of("SUCCESS", postService.getPosts(page)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.of("SUCCESS", postService.getPost(postId)));
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> createPost(
            @RequestBody @Valid CreatePostRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        postService.createPost(userId, request);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @PatchMapping("/{postId}")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        postService.updatePost(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @DeleteMapping("/{postId}")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        postService.deletePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.of("SUCCESS"));
    }

    @PutMapping("/{postId}/likes")
    @RequireAuth
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        postService.toggleLike(postId, userId);
        return ResponseEntity.ok(ApiResponse.of("LIKE_UPDATED"));
    }
}
