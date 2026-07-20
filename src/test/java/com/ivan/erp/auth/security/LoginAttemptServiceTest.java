package com.ivan.erp.auth.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    @Test
    void successfulLoginsDoNotIncrementTheCounter() {
        LoginAttemptService service = new LoginAttemptService(true, 3, 15, 100);

        for (int i = 0; i < 20; i++) {
            service.recordSuccess("127.0.0.1", "user@example.com");
        }

        assertThat(service.isBlocked("127.0.0.1", "user@example.com")).isFalse();
    }

    @Test
    void failuresBlockByNormalizedIpAndUsernameAndSuccessClearsThem() {
        LoginAttemptService service = new LoginAttemptService(true, 3, 15, 100);
        service.recordFailure("127.0.0.1", "USER@example.com");
        service.recordFailure("127.0.0.1", " user@example.com ");
        service.recordFailure("127.0.0.1", "user@EXAMPLE.COM");

        assertThat(service.isBlocked("127.0.0.1", "user@example.com")).isTrue();

        service.recordSuccess("127.0.0.1", "USER@example.com");
        assertThat(service.isBlocked("127.0.0.1", "user@example.com")).isFalse();
    }
}
