package com.ivan.erp.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class LoginAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public LoginAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        super("/login?error");
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        loginAttemptService.recordFailure(request.getRemoteAddr(), request.getParameter("username"));
        super.onAuthenticationFailure(request, response, exception);
    }
}
