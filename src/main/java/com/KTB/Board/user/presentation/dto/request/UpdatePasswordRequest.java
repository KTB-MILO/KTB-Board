package com.KTB.Board.user.presentation.dto.request;

import com.KTB.Board.user.presentation.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest (
        @NotBlank(message = "{password.required}")
        @Size(min=8, max=20, message = "{password.size")
        @ValidPassword String password
){}
