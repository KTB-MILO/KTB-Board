package com.KTB.Board.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", 400),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", 401),
    UNAUTHORIZED_USER("UNAUTHORIZED_USER", 401),
    INVALID_NICKNAME("INVALID_NICKNAME", 400),
    INVALID_PASSWORD("INVALID_PASSWORD", 400),
    BOARD_NOT_FOUND("BOARD_NOT_FOUND", 404),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500);

    private final String code;
    private final int status;
}
