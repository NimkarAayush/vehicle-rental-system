package com.vehiclerental.controller;

import com.vehiclerental.entity.User;
import com.vehiclerental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserService userService;

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        addUserToModel(model);
        return "admin/dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model) {
        addUserToModel(model);
        return "customer/dashboard";
    }
    
    private void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("currentUser", user);
    }
}
