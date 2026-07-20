package com.ivan.erp.shared.bootstrap;

import com.ivan.erp.user.Role;
import com.ivan.erp.user.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @Test
    void createsOnlyMissingRequiredRoles() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        new RoleInitializer(roleRepository).run();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().stream().map(Role::getName).toList())
                .containsExactlyInAnyOrder("MANAGER", "USER");
    }
}
