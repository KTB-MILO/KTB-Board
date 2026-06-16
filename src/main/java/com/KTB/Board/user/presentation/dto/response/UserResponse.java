package com.KTB.Board.user.presentation.dto.response;

import com.KTB.Board.user.entity.User;

public record UserResponse (
    Long userId,
    String userName,
    String email,
    String profileImage
){
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }
}
