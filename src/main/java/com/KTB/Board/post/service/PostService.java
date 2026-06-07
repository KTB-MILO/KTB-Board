package com.KTB.Board.post.service;

import com.KTB.Board.common.exception.CustomException;
import com.KTB.Board.common.exception.ErrorCode;
import com.KTB.Board.common.response.PageResponse;
import com.KTB.Board.post.dto.*;
import com.KTB.Board.post.entity.Post;
import com.KTB.Board.post.entity.PostLike;
import com.KTB.Board.post.repository.PostLikeRepository;
import com.KTB.Board.post.repository.PostRepository;
import com.KTB.Board.user.entity.User;
import com.KTB.Board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getPosts(int page) {
        Page<Post> postPage = postRepository.findByDeletedAtIsNull(
                PageRequest.of(page - 1, 10, Sort.by("createdAt").descending()));
        List<PostListResponse> content = postPage.getContent().stream()
                .map(PostListResponse::new)
                .toList();
        return new PageResponse<>(content, postPage.getTotalPages(), postPage.getTotalElements(), page);
    }

    @Transactional
    public PostDetailResponse getPost(Long postId) {
        Post post = findPost(postId);
        postRepository.incrementViewCount(postId);
        return new PostDetailResponse(post);
    }

    @Transactional
    public void createPost(Long userId, CreatePostRequest request) {
        User user = findUser(userId);
        postRepository.save(Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .image(request.getImage())
                .build());
    }

    @Transactional
    public void updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = findPost(postId);
        checkOwner(post.getUser().getId(), userId);
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getImage() != null) post.setImage(request.getImage());
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = findPost(postId);
        checkOwner(post.getUser().getId(), userId);
        // soft delete: cascade DELETE를 피해 comments/likes 대량 삭제로 인한 락 방지
        post.setDeletedAt(LocalDateTime.now());
    }

    @Transactional
    public void toggleLike(Long postId, Long userId) {
        Post post = findPost(postId);
        User user = findUser(userId);
        postLikeRepository.findByPostAndUser(post, user).ifPresentOrElse(
                like -> {
                    postLikeRepository.delete(like);
                    postRepository.decrementLikeCount(postId);
                },
                () -> {
                    postLikeRepository.save(PostLike.builder().post(post).user(user).build());
                    postRepository.incrementLikeCount(postId);
                }
        );
    }

    private Post findPost(Long postId) {
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    private void checkOwner(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
    }
}
