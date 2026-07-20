package com.ivan.erp.shared.bootstrap;

import com.ivan.erp.user.Role;
import com.ivan.erp.user.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the application roles exist without creating any user accounts.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoleInitializer implements CommandLineRunner {

    private static final String[] REQUIRED_ROLES = {"ADMIN", "MANAGER", "USER"};

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (String roleName : REQUIRED_ROLES) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
        }
    }
}
