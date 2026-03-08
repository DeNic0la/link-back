package ch.denic0la.validation.annotations;

import ch.denic0la.validation.SecondFactorKeyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SecondFactorKeyValidator.class)
public @interface SecondFactorKey {
    int length() default 6;
    boolean allowNull() default true;
    String message() default "If provided, must match the required pattern and not be empty";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}