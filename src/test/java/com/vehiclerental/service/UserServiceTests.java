package com.vehiclerental.service;

import com.vehiclerental.dto.UserRegistrationDto;
import com.vehiclerental.entity.Role;
import com.vehiclerental.entity.User;
import com.vehiclerental.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testSaveUser_convertsEmailToLowercaseAndAssignsCustomerRole() {
        UserRegistrationDto dto = new UserRegistrationDto("Jane", "Doe", "JANE@Example.com", "pass1234", "pass1234");
        
        when(passwordEncoder.encode(any())).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User savedUser = userService.save(dto);

        assertNotNull(savedUser);
        assertEquals("jane@example.com", savedUser.getEmail());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertEquals("hashedPass", savedUser.getPassword());
        
        verify(userRepository, times(1)).save(any(User.class));
    }
}
