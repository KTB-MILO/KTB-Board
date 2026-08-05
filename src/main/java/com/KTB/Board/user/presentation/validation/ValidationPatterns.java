package com.KTB.Board.user.presentation.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {}

    public static final String PASSWORD =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()]).{8,20}$";
}