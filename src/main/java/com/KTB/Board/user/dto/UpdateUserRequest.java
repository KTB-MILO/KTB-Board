package com.KTB.Board.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateUserRequest {

    @NotBlank
    private String userName;

    private String profileImage;
}
