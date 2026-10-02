package com.vehiclerental.controller;

import com.vehiclerental.entity.Booking;
import com.vehiclerental.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public String listBookings(@RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "status", required = false) String statusFilter,
                               Model model) {
        
        Page<Booking> bookingPage = bookingService.findAll(statusFilter, PageRequest.of(page, 10, Sort.by("createdAt").descending()));
        
        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("statusFilter", statusFilter);
        
        return "admin/bookings/list";
    }

    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/start")
    public String startBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.startBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rental started successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/complete")
    public String completeBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.completeBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rental completed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBookingAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
}
