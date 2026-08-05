package com.KTB.Board.auth.service;

import com.KTB.Board.common.exception.CustomException;
import com.KTB.Board.common.exception.ErrorCode;
import com.KTB.Board.common.util.PasswordUtil;
import com.KTB.Board.session.entity.UserSession;
import com.KTB.Board.session.repository.UserSessionRepository;
import com.KTB.Board.user.entity.User;
import com.KTB.Board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;

    @Transactional
    public String login(String email, String password) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String sessionId = UUID.randomUUID().toString();
        sessionRepository.save(UserSession.builder()
                .sessionId(sessionId)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        return sessionId;
    }

    @Transactional
    public void logout(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
