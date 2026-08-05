package com.KTB.Board.user.service;

import com.KTB.Board.common.exception.CustomException;
import com.KTB.Board.common.exception.ErrorCode;
import com.KTB.Board.common.util.PasswordUtil;
import com.KTB.Board.session.repository.UserSessionRepository;
import com.KTB.Board.user.entity.User;
import com.KTB.Board.user.presentation.dto.request.SignupRequest;
import com.KTB.Board.user.presentation.dto.request.UpdatePasswordRequest;
import com.KTB.Board.user.presentation.dto.request.UpdateUserRequest;
import com.KTB.Board.user.presentation.dto.response.UserResponse;
import com.KTB.Board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }
        userRepository.save(User.builder()
                .email(request.email())
                .password(PasswordUtil.hash(request.password()))
                .nickname(request.nickname())
                .profileImageUrl(request.profileImageUrl())
                .build());
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public void updateUser(Long userId, Long authenticatedUserId, UpdateUserRequest request) {
        if (!userId.equals(authenticatedUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        User user = findUser(userId);
        user.setNickname(request.nickname());
        if (request.profileImage() != null) {
            user.setProfileImageUrl(request.profileImage());
        }
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = findUser(userId);
        user.setPassword(PasswordUtil.hash(request.password()));
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
