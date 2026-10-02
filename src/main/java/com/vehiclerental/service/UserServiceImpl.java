package com.vehiclerental.service;

import com.vehiclerental.dto.UserRegistrationDto;
import com.vehiclerental.entity.Role;
import com.vehiclerental.entity.User;
import com.vehiclerental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User save(UserRegistrationDto registrationDto) {
        User user = User.builder()
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .email(registrationDto.getEmail().toLowerCase())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .role(Role.CUSTOMER) // Never bind role from form, always CUSTOMER for public registration
                .build();
                
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }
}
