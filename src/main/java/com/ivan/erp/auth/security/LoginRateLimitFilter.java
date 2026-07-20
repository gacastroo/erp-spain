package com.ivan.erp.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rejects only credentials that already exceeded the failed-login limit.
 * Failed attempts are recorded by the authentication failure handler, while a
 * successful authentication clears the matching counter.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String username = request.getParameter("username");
        if (loginAttemptService.isBlocked(request.getRemoteAddr(), username)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
