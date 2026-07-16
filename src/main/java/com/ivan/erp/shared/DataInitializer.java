package com.ivan.erp.shared;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.CompanyRepository;
import com.ivan.erp.user.AppUser;
import com.ivan.erp.user.AppUserRepository;
import com.ivan.erp.user.Role;
import com.ivan.erp.user.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            AppUserRepository appUserRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> roleRepository.save(new Role("MANAGER")));

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));

        if (!appUserRepository.existsByEmailIgnoreCase("admin@erp.local")) {
            AppUser admin = new AppUser(
                    "Administrador",
                    "ERP",
                    "admin@erp.local",
                    passwordEncoder.encode("Admin123!")
            );

            admin.addRole(adminRole);
            admin.addRole(managerRole);
            admin.addRole(userRole);

            appUserRepository.save(admin);
        }

        companyRepository.findByTaxIdIgnoreCase("B00000000")
                .orElseGet(() -> companyRepository.save(
                        new Company("Empresa Demo S.L.", "B00000000")
                ));
    }
}
