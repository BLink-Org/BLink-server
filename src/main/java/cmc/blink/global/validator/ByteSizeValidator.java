package cmc.blink.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class ByteSizeValidator implements ConstraintValidator<ByteSize, String> {

    private int max;

    @Override
    public void initialize(ByteSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
