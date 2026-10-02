package com.vehiclerental.controller;

import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<Vehicle> availableVehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
        model.addAttribute("vehicles", availableVehicles);
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("totalUsers", userRepository.count());
        return "index";
    }
}
