package com.ivan.erp.shared.bootstrap;

import com.ivan.erp.user.AppUser;
import com.ivan.erp.user.AppUserRepository;
import com.ivan.erp.user.Role;
import com.ivan.erp.user.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Optional one-time administrator bootstrap.
 *
 * <p>Disabled by default. Enable it explicitly with INITIAL_ADMIN_ENABLED=true
 * and provide the email/password through environment variables or a secret
 * manager. No credentials are embedded in the application.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(prefix = "app.bootstrap.admin", name = "enabled", havingValue = "true")
public class InitialAdminInitializer implements CommandLineRunner {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> FORBIDDEN_PASSWORDS = Set.of(
            "admin123!",
            "password",
            "password123!",
            "changeme123!",
            "change-this-password"
    );

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String rawPassword;
    private final String firstName;
    private final String lastName;

    public InitialAdminInitializer(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.email:}") String email,
            @Value("${app.bootstrap.admin.password:}") String rawPassword,
            @Value("${app.bootstrap.admin.first-name:Administrador}") String firstName,
            @Value("${app.bootstrap.admin.last-name:ERP}") String lastName
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = normalize(email);
        this.rawPassword = rawPassword == null ? "" : rawPassword;
        this.firstName = normalizeName(firstName, "Administrador");
        this.lastName = normalizeName(lastName, "ERP");
    }

    @Override
    @Transactional
    public void run(String... args) {
        validateConfiguration();

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        AppUser admin = new AppUser(
                firstName,
                lastName,
                email,
                passwordEncoder.encode(rawPassword)
        );

        admin.addRole(requiredRole("ADMIN"));
        admin.addRole(requiredRole("MANAGER"));
        admin.addRole(requiredRole("USER"));
        appUserRepository.save(admin);
    }

    private Role requiredRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Required role '" + roleName + "' was not initialized"
                ));
    }

    private void validateConfiguration() {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_EMAIL must contain a valid email address when initial admin bootstrap is enabled"
            );
        }

        String normalizedPassword = rawPassword.toLowerCase(Locale.ROOT);
        boolean strongEnough = rawPassword.length() >= 12
                && rawPassword.chars().anyMatch(Character::isUpperCase)
                && rawPassword.chars().anyMatch(Character::isLowerCase)
                && rawPassword.chars().anyMatch(Character::isDigit)
                && rawPassword.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        if (!strongEnough || FORBIDDEN_PASSWORDS.contains(normalizedPassword)) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_PASSWORD must be at least 12 characters and include upper-case, lower-case, digit and symbol"
            );
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeName(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
