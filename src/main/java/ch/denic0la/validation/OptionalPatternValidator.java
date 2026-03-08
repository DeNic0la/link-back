package ch.denic0la.validation;

import ch.denic0la.validation.annotations.OptionalPattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalPatternValidator implements ConstraintValidator<OptionalPattern, String> {
    private String regex;

    @Override
    public void initialize(OptionalPattern constraintAnnotation) {
        this.regex = constraintAnnotation.regexp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 1. Allow null
        if (value == null) {
            return true;
        }
        // 2. Disallow empty or whitespace-only
        if (value.trim().isEmpty()) {
            return false;
        }
        // 3. Check pattern
        return value.matches(regex);
    }
}