package com.vehiclerental.controller;

import com.vehiclerental.dto.UserRegistrationDto;
import com.vehiclerental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto, 
                                      BindingResult result, 
                                      Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match!");
            return "register";
        }

        if (userService.findByEmail(registrationDto.getEmail()) != null) {
            result.rejectValue("email", "error.user", "There is already an account registered with that email!");
            return "register";
        }

        userService.save(registrationDto);
        return "redirect:/register?success";
    }
    
    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
