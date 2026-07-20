package com.ivan.erp.quote.web.validation;

import com.ivan.erp.quote.web.QuoteLineForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class QuoteLineValidator implements ConstraintValidator<ValidQuoteLine, QuoteLineForm> {

    @Override
    public boolean isValid(QuoteLineForm line, ConstraintValidatorContext context) {
        if (line == null || line.isEmpty()) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (line.getDescription() == null || line.getDescription().isBlank()) {
            addViolation(context, "description", "La descripción es obligatoria");
            valid = false;
        } else if (line.getDescription().length() > 500) {
            addViolation(context, "description", "La descripción no puede superar los 500 caracteres");
            valid = false;
        }

        if (!validDecimal(line.getQuantity(), new BigDecimal("0.01"), null, 10, 2)) {
            addViolation(context, "quantity", "La cantidad debe ser mayor que 0 y tener como máximo 2 decimales");
            valid = false;
        }
        if (!validDecimal(line.getUnitPrice(), BigDecimal.ZERO, null, 10, 2)) {
            addViolation(context, "unitPrice", "El precio no puede ser negativo y debe tener como máximo 2 decimales");
            valid = false;
        }
        if (!validDecimal(line.getVatRate(), BigDecimal.ZERO, new BigDecimal("100.00"), 3, 2)) {
            addViolation(context, "vatRate", "El IVA debe estar entre 0 y 100 y tener como máximo 2 decimales");
            valid = false;
        }

        return valid;
    }

    private boolean validDecimal(BigDecimal value, BigDecimal min, BigDecimal max, int integerDigits, int fractionDigits) {
        if (value == null || value.compareTo(min) < 0 || (max != null && value.compareTo(max) > 0)) {
            return false;
        }
        int integerPart = Math.max(0, value.precision() - value.scale());
        return value.scale() <= fractionDigits && integerPart <= integerDigits;
    }

    private void addViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
