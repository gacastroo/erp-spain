package com.ivan.erp.quote.web.validation;

import com.ivan.erp.quote.web.QuoteLineForm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteLineValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void completelyEmptyDefaultLineIsIgnored() {
        assertThat(validator.validate(new QuoteLineForm())).isEmpty();
    }

    @Test
    void partiallyCompletedLineStillRequiresDescription() {
        QuoteLineForm line = new QuoteLineForm();
        line.setUnitPrice(new BigDecimal("10.00"));

        assertThat(validator.validate(line).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .toList())
                .contains("description");
    }
}
