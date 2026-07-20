package com.ivan.erp.auth.config;

import com.ivan.erp.auth.security.LoginAttemptService;
import com.ivan.erp.auth.security.LoginAuthenticationFailureHandler;
import com.ivan.erp.auth.security.LoginAuthenticationSuccessHandler;
import com.ivan.erp.auth.security.LoginRateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final boolean rememberMeEnabled;
    private final String rememberMeKey;

    public SecurityConfig(
            @Value("${app.security.remember-me.enabled:false}") boolean rememberMeEnabled,
            @Value("${app.security.remember-me.key:}") String rememberMeKey
    ) {
        this.rememberMeEnabled = rememberMeEnabled;
        this.rememberMeKey = rememberMeKey == null ? "" : rememberMeKey.trim();
        validateRememberMeConfiguration();
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter(LoginAttemptService loginAttemptService) {
        return new LoginRateLimitFilter(loginAttemptService);
    }

    @Bean
    public LoginAuthenticationFailureHandler loginAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        return new LoginAuthenticationFailureHandler(loginAttemptService);
    }

    @Bean
    public LoginAuthenticationSuccessHandler loginAuthenticationSuccessHandler(LoginAttemptService loginAttemptService) {
        return new LoginAuthenticationSuccessHandler(loginAttemptService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoginRateLimitFilter loginRateLimitFilter,
            LoginAuthenticationSuccessHandler successHandler,
            LoginAuthenticationFailureHandler failureHandler
    ) throws Exception {
        http
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/login"
                        ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .headers(headers -> headers
                        .cacheControl(Customizer.withDefaults())
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "font-src 'self'; " +
                                        "img-src 'self' data:; " +
                                        "connect-src 'self'; " +
                                        "object-src 'none'; " +
                                        "frame-ancestors 'self'; " +
                                        "base-uri 'self'; " +
                                        "form-action 'self'"
                        ))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                        .httpStrictTransportSecurity(Customizer.withDefaults())
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()"))
                );

        if (rememberMeEnabled) {
            http.rememberMe(remember -> remember
                    .key(rememberMeKey)
                    .tokenValiditySeconds(60 * 60 * 24 * 7)
            );
        }

        return http.build();
    }

    private void validateRememberMeConfiguration() {
        if (!rememberMeEnabled) {
            return;
        }
        if (rememberMeKey.length() < 32) {
            throw new IllegalStateException(
                    "REMEMBER_ME_KEY must contain at least 32 characters when remember-me is enabled"
            );
        }
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
