package com.KTB.Board.user.presentation.dto.request;

import com.KTB.Board.user.presentation.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest (

    @NotBlank(message = "{email.required}")
    @Email(message = "{email.invalid}")
    String email,

    @NotBlank(message = "{password.required}")
    @Size(min=8, max=20, message = "{password.size")
    @ValidPassword
    String password,

    @NotBlank(message = "{nickname.required}")
    @Size(max = 10, message = "{nickname.size]")
    @Pattern(
            regexp = "^\\S+$",
            message = "{nickname.invalid}"
    )
    String nickname,

    String profileImageUrl
){}
