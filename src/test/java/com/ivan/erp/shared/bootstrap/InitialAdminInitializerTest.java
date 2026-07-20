package com.ivan.erp.shared.bootstrap;

import com.ivan.erp.user.AppUser;
import com.ivan.erp.user.AppUserRepository;
import com.ivan.erp.user.Role;
import com.ivan.erp.user.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialAdminInitializerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsAdministratorOnlyFromExplicitStrongCredentials() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(new Role("MANAGER")));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(new Role("USER")));
        when(appUserRepository.existsByEmailIgnoreCase("owner@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Unique-Password-2026!")).thenReturn("bcrypt-hash");

        InitialAdminInitializer initializer = new InitialAdminInitializer(
                appUserRepository,
                roleRepository,
                passwordEncoder,
                " Owner@Example.com ",
                "Unique-Password-2026!",
                " Ana ",
                " Admin "
        );

        initializer.run();

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());

        AppUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("owner@example.com");
        assertThat(saved.getPassword()).isEqualTo("bcrypt-hash");
        assertThat(saved.getFirstName()).isEqualTo("Ana");
        assertThat(saved.getLastName()).isEqualTo("Admin");
        assertThat(saved.getRoles().stream().map(Role::getName).toList())
                .containsExactlyInAnyOrder("ADMIN", "MANAGER", "USER");
    }

    @Test
    void doesNotOverwriteAnExistingAdministrator() {
        when(appUserRepository.existsByEmailIgnoreCase("owner@example.com")).thenReturn(true);

        InitialAdminInitializer initializer = new InitialAdminInitializer(
                appUserRepository,
                roleRepository,
                passwordEncoder,
                "owner@example.com",
                "Unique-Password-2026!",
                "Owner",
                "ERP"
        );

        initializer.run();

        verify(passwordEncoder, never()).encode("Unique-Password-2026!");
        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }

    @Test
    void rejectsWeakOrKnownPasswordsBeforeCreatingAUser() {
        InitialAdminInitializer initializer = new InitialAdminInitializer(
                appUserRepository,
                roleRepository,
                passwordEncoder,
                "owner@example.com",
                "Admin123!",
                "Owner",
                "ERP"
        );

        assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INITIAL_ADMIN_PASSWORD");

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }

    @Test
    void rejectsMissingOrInvalidEmailBeforeCreatingAUser() {
        InitialAdminInitializer initializer = new InitialAdminInitializer(
                appUserRepository,
                roleRepository,
                passwordEncoder,
                "not-an-email",
                "Unique-Password-2026!",
                "Owner",
                "ERP"
        );

        assertThatThrownBy(initializer::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INITIAL_ADMIN_EMAIL");

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
    }
}
