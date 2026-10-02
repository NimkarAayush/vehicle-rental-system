package com.vehiclerental.controller;

import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleType;
import com.vehiclerental.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class PublicVehicleController {

    private final VehicleService vehicleService;
    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList("pricePerDay", "brand");

    @GetMapping
    public String listAvailableVehicles(
            @RequestParam(required = false) VehicleType type,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "pricePerDay") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            sortField = "pricePerDay";
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Vehicle> vehiclePage = vehicleService.findAvailableFiltered(type, brand, minPrice, maxPrice, pageable);

        model.addAttribute("vehiclePage", vehiclePage);
        model.addAttribute("typeFilter", type);
        model.addAttribute("brandFilter", brand);
        model.addAttribute("minPriceFilter", minPrice);
        model.addAttribute("maxPriceFilter", maxPrice);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("types", VehicleType.values());

        return "vehicles/list";
    }

    @GetMapping("/{id}")
    public String viewVehicleDetails(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        model.addAttribute("vehicle", vehicle);
        return "vehicles/details";
    }
}
