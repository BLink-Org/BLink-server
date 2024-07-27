package cmc.blink.global.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ByteSizeValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ByteSize {

    String message()default "The field exceeds the maximum byte size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int max();

}
