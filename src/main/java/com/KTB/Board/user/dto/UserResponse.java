package com.KTB.Board.user.dto;

import com.KTB.Board.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long userId;
    private final String userName;
    private final String email;
    private final String profileImage;

    public UserResponse(User user) {
        this.userId = user.getId();
        this.userName = user.getNickname();
        this.email = user.getEmail();
        this.profileImage = user.getProfileImageUrl();
    }
}
