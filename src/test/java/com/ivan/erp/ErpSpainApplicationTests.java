package com.ivan.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class ErpSpainApplicationTests {

    @Test
    void applicationEnablesSpringBootAndScheduling() {
        assertThat(ErpSpainApplication.class).hasAnnotation(SpringBootApplication.class);
        assertThat(ErpSpainApplication.class).hasAnnotation(EnableScheduling.class);
    }
}
