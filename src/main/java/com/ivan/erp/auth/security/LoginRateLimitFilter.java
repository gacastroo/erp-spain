package com.ivan.erp.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final int maxAttempts;
    private final Duration window;
    private final Map<String, AttemptWindow> attemptsByIp = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(
            boolean enabled,
            int maxAttempts,
            long windowMinutes
    ) {
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled
                || !"POST".equalsIgnoreCase(request.getMethod())
                || !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = request.getRemoteAddr();
        Instant now = Instant.now();

        AttemptWindow windowState = attemptsByIp.compute(key, (ignored, current) -> {
            if (current == null || current.isExpired(now, window)) {
                return new AttemptWindow(now);
            }
            current.increment();
            return current;
        });

        cleanExpiredWindows(now);

        if (windowState.count() > maxAttempts) {
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Demasiados intentos de acceso. Espera unos minutos antes de volver a intentarlo.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void cleanExpiredWindows(Instant now) {
        attemptsByIp.entrySet().removeIf(entry -> entry.getValue().isExpired(now, window.multipliedBy(2)));
    }

    private static final class AttemptWindow {
        private final Instant startedAt;
        private final AtomicInteger count = new AtomicInteger(1);

        private AttemptWindow(Instant startedAt) {
            this.startedAt = startedAt;
        }

        private void increment() {
            count.incrementAndGet();
        }

        private int count() {
            return count.get();
        }

        private boolean isExpired(Instant now, Duration duration) {
            return startedAt.plus(duration).isBefore(now);
        }
    }
}
