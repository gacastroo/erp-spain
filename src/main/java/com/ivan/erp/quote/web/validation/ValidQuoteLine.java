package com.ivan.erp.quote.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = QuoteLineValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQuoteLine {
    String message() default "La línea de presupuesto no es válida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
