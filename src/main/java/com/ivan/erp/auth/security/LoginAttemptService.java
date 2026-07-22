package com.ivan.erp.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final boolean enabled;
    private final int maxAttempts;
    private final int maxTrackedEntries;
    private final Duration window;
    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${app.security.login-rate-limit.enabled:true}")
            boolean enabled,

            @Value("${app.security.login-rate-limit.max-attempts:10}")
            int maxAttempts,

            @Value("${app.security.login-rate-limit.window-minutes:15}")
            long windowMinutes,

            @Value("${app.security.login-rate-limit.max-tracked-entries:10000}")
            int maxTrackedEntries
    ) {
        this(
                enabled,
                maxAttempts,
                windowMinutes,
                maxTrackedEntries,
                Clock.systemUTC()
        );
    }

    LoginAttemptService(
            boolean enabled,
            int maxAttempts,
            long windowMinutes,
            int maxTrackedEntries,
            Clock clock
    ) {
        if (maxAttempts < 1 || windowMinutes < 1 || maxTrackedEntries < 100) {
            throw new IllegalArgumentException(
                    "Invalid login rate limit configuration"
            );
        }

        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.maxTrackedEntries = maxTrackedEntries;
        this.window = Duration.ofMinutes(windowMinutes);
        this.clock = clock;
    }

    public boolean isBlocked(String remoteAddress, String username) {
        if (!enabled) {
            return false;
        }

        String key = key(remoteAddress, username);
        Instant now = clock.instant();
        AttemptWindow current = attempts.get(key);

        if (current == null) {
            return false;
        }

        if (current.isExpired(now, window)) {
            attempts.remove(key, current);
            return false;
        }

        return current.count() >= maxAttempts;
    }

    public void recordFailure(String remoteAddress, String username) {
        if (!enabled) {
            return;
        }

        Instant now = clock.instant();

        ensureCapacity(now);

        attempts.compute(key(remoteAddress, username), (ignored, current) -> {
            if (current == null || current.isExpired(now, window)) {
                return new AttemptWindow(now, 1);
            }

            return current.incremented();
        });
    }

    public void recordSuccess(String remoteAddress, String username) {
        if (enabled) {
            attempts.remove(key(remoteAddress, username));
        }
    }

    private void ensureCapacity(Instant now) {
        if (attempts.size() < maxTrackedEntries) {
            return;
        }

        attempts.entrySet().removeIf(
                entry -> entry.getValue().isExpired(now, window)
        );

        if (attempts.size() < maxTrackedEntries) {
            return;
        }

        attempts.entrySet()
                .stream()
                .min(Comparator.comparing(
                        entry -> entry.getValue().startedAt()
                ))
                .ifPresent(
                        entry -> attempts.remove(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
    }

    private String key(String remoteAddress, String username) {
        String normalizedIp =
                remoteAddress == null || remoteAddress.isBlank()
                        ? "unknown"
                        : remoteAddress.trim();

        String normalizedUsername =
                username == null
                        ? ""
                        : username.trim().toLowerCase(Locale.ROOT);

        return normalizedIp + "|" + normalizedUsername;
    }

    private record AttemptWindow(
            Instant startedAt,
            int count
    ) {

        AttemptWindow incremented() {
            return new AttemptWindow(startedAt, count + 1);
        }

        boolean isExpired(Instant now, Duration duration) {
            return !now.isBefore(startedAt.plus(duration));
        }
    }
}