package com.KTB.Board.user.service;

import com.KTB.Board.comment.repository.CommentRepository;
import com.KTB.Board.common.exception.CustomException;
import com.KTB.Board.common.exception.ErrorCode;
import com.KTB.Board.common.util.PasswordUtil;
import com.KTB.Board.post.repository.PostLikeRepository;
import com.KTB.Board.post.repository.PostRepository;
import com.KTB.Board.session.repository.UserSessionRepository;
import com.KTB.Board.user.dto.*;
import com.KTB.Board.user.entity.User;
import com.KTB.Board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        userRepository.save(User.builder()
                .email(request.getEmail())
                .password(PasswordUtil.hash(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .build());
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return new UserResponse(findUser(userId));
    }

    @Transactional
    public void updateUser(Long userId, Long authenticatedUserId, UpdateUserRequest request) {
        if (!userId.equals(authenticatedUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        User user = findUser(userId);
        user.setNickname(request.getUserName());
        if (request.getProfileImage() != null) {
            user.setProfileImageUrl(request.getProfileImage());
        }
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = findUser(userId);
        user.setPassword(PasswordUtil.hash(request.getPassword()));
    }

    @Transactional
    public void deleteUser(Long userId, Long authenticatedUserId) {
        if (!userId.equals(authenticatedUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        User user = findUser(userId);
        sessionRepository.deleteByUserId(userId);
        user.softDelete();
    }

    private User findUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }
}
