package com.vehiclerental.controller;

import com.vehiclerental.dto.VehicleFormDto;
import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.entity.VehicleType;
import com.vehiclerental.exception.VehicleInUseException;
import com.vehiclerental.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList("id", "brand", "model", "pricePerDay", "status");

    @GetMapping
    public String listVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            sortField = "id";
        }
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Vehicle> vehiclePage = vehicleService.findAllFiltered(search, status, pageable);
        
        model.addAttribute("vehiclePage", vehiclePage);
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", status);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("statuses", VehicleStatus.values());
        
        return "admin/vehicles/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("vehicleDto", new VehicleFormDto());
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("statuses", VehicleStatus.values());
        return "admin/vehicles/form";
    }

    @PostMapping("/new")
    public String addVehicle(@Valid @ModelAttribute("vehicleDto") VehicleFormDto vehicleDto, 
                             BindingResult result, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
                             
        if (!vehicleService.isRegistrationNumberUnique(vehicleDto.getRegistrationNumber(), null)) {
            result.rejectValue("registrationNumber", "error.vehicle", "Registration number already exists");
        }

        if (result.hasErrors()) {
            model.addAttribute("types", VehicleType.values());
            model.addAttribute("statuses", VehicleStatus.values());
            return "admin/vehicles/form";
        }

        vehicleService.save(vehicleDto);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle added successfully!");
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        VehicleFormDto dto = new VehicleFormDto();
        dto.setId(vehicle.getId());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setType(vehicle.getType());
        dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setPricePerDay(vehicle.getPricePerDay());
        dto.setStatus(vehicle.getStatus());
        dto.setCurrentImageUrl(vehicle.getImageUrl());
        
        model.addAttribute("vehicleDto", dto);
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("statuses", VehicleStatus.values());
        return "admin/vehicles/form";
    }

    @PostMapping("/edit/{id}")
    public String editVehicle(@PathVariable Long id, 
                              @Valid @ModelAttribute("vehicleDto") VehicleFormDto vehicleDto, 
                              BindingResult result, 
                              Model model, 
                              RedirectAttributes redirectAttributes) {
                              
        if (!vehicleService.isRegistrationNumberUnique(vehicleDto.getRegistrationNumber(), id)) {
            result.rejectValue("registrationNumber", "error.vehicle", "Registration number already exists");
        }

        if (result.hasErrors()) {
            model.addAttribute("types", VehicleType.values());
            model.addAttribute("statuses", VehicleStatus.values());
            return "admin/vehicles/form";
        }

        vehicleService.update(id, vehicleDto);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle updated successfully!");
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        model.addAttribute("vehicle", vehicle);
        return "admin/vehicles/delete-confirm";
    }

    @PostMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehicleService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle deleted successfully!");
        } catch (VehicleInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam VehicleStatus status, RedirectAttributes redirectAttributes) {
        vehicleService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Status updated successfully!");
        return "redirect:/admin/vehicles";
    }
}
