package com.vehiclerental.service;

import com.vehiclerental.dto.UserRegistrationDto;
import com.vehiclerental.entity.User;

public interface UserService {
    User save(UserRegistrationDto registrationDto);
    User findByEmail(String email);
}
