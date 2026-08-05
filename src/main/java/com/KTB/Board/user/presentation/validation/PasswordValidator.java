package com.KTB.Board.user.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String value,
                           ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return value.matches(ValidationPatterns.PASSWORD);
    }
}
