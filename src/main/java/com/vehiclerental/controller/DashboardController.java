package com.vehiclerental.controller;

import com.vehiclerental.entity.BookingStatus;
import com.vehiclerental.entity.User;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.repository.UserRepository;
import com.vehiclerental.repository.VehicleRepository;
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
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        addUserToModel(model);
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("activeBookings", bookingRepository.countByStatus(BookingStatus.ACTIVE));
        return "admin/dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model) {
        User user = addUserToModel(model);
        model.addAttribute("recentBookings", bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        return "customer/dashboard";
    }
    
    private User addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("currentUser", user);
        return user;
    }
}
