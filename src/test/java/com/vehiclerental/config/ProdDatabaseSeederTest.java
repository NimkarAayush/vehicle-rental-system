package com.vehiclerental.config;

import com.vehiclerental.entity.Role;
import com.vehiclerental.entity.User;
import com.vehiclerental.repository.UserRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdDatabaseSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment env;

    private ProdDatabaseSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new ProdDatabaseSeeder(userRepository, vehicleRepository, passwordEncoder, env);
    }

    @Test
    void run_MissingAdminEmail_ThrowsException() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(env.getProperty("ADMIN_EMAIL")).thenReturn(null);
        when(env.getProperty("ADMIN_PASSWORD")).thenReturn("validpassword123");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seeder.run());
        assertTrue(exception.getMessage().contains("missing"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_MissingAdminPassword_ThrowsException() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(env.getProperty("ADMIN_EMAIL")).thenReturn("admin@test.com");
        when(env.getProperty("ADMIN_PASSWORD")).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seeder.run());
        assertTrue(exception.getMessage().contains("missing"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_ShortAdminPassword_ThrowsException() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(env.getProperty("ADMIN_EMAIL")).thenReturn("admin@test.com");
        when(env.getProperty("ADMIN_PASSWORD")).thenReturn("short");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seeder.run());
        assertTrue(exception.getMessage().contains("at least 12 characters long"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_ExistingAdmin_DoesNotCreateNewAdmin() throws Exception {
        User existingAdmin = User.builder().email("admin@old.com").role(Role.ADMIN).build();
        when(userRepository.findAll()).thenReturn(List.of(existingAdmin));

        seeder.run();

        verify(env, never()).getProperty("ADMIN_EMAIL");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_ExistingCustomerNoAdmin_CreatesAdmin() throws Exception {
        User existingCustomer = User.builder().email("cust@old.com").role(Role.CUSTOMER).build();
        when(userRepository.findAll()).thenReturn(List.of(existingCustomer));
        when(env.getProperty("ADMIN_EMAIL")).thenReturn("admin@test.com");
        when(env.getProperty("ADMIN_PASSWORD")).thenReturn("verysecurepassword");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(env.getProperty("APP_SEED_DEMO_VEHICLES", "false")).thenReturn("false");

        seeder.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("admin@test.com", savedUser.getEmail());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertEquals("hashed_password", savedUser.getPassword());
    }
}
