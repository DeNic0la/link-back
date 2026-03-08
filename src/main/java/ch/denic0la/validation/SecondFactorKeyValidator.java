package ch.denic0la.validation;

import ch.denic0la.validation.annotations.OptionalPattern;
import ch.denic0la.validation.annotations.SecondFactorKey;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SecondFactorKeyValidator implements ConstraintValidator<SecondFactorKey, String> {
    private int length;
    private boolean allowNull;

    @Override
    public void initialize(SecondFactorKey constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.length = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 1. Allow null
        if (value == null) {
            return this.allowNull;
        }
        // 2. Require exact length
        if (value.trim().length()
        != this.length) {
            return false;
        }

        return value.chars().allMatch(Character::isDigit);
    }
}