package com.KTB.Board.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest (

    @NotBlank(message = "{nickname.required}")
    @Size(max = 10, message = "{nickname.size]")
    @Pattern(
            regexp = "^\\S+$",
            message = "{nickname.invalid}"
    )
    String nickname,

    String profileImage
){}
