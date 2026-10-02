package com.vehiclerental.controller;

import com.vehiclerental.dto.BookingFormDto;
import com.vehiclerental.entity.Booking;
import com.vehiclerental.entity.User;
import com.vehiclerental.exception.BookingConflictException;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.service.BookingService;
import com.vehiclerental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/customer/bookings")
@RequiredArgsConstructor
public class CustomerBookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @GetMapping
    public String listBookings(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("bookings", bookings);
        return "customer/bookings/list";
    }

    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model, Principal principal) {
        Booking booking = bookingService.findById(id);
        User user = userService.findByEmail(principal.getName());
        
        if (!booking.getUser().getId().equals(user.getId())) {
            return "redirect:/403";
        }
        
        model.addAttribute("booking", booking);
        return "customer/bookings/details";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam("vehicleId") Long vehicleId,
                                @Valid @ModelAttribute("bookingDto") BookingFormDto bookingDto,
                                BindingResult result,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid dates provided.");
            return "redirect:/vehicles/" + vehicleId;
        }

        try {
            User user = userService.findByEmail(principal.getName());
            Booking booking = bookingService.createBooking(vehicleId, user, bookingDto);
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully!");
            return "redirect:/customer/bookings/" + booking.getId();
        } catch (BookingConflictException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles/" + vehicleId;
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "This vehicle was just booked by someone else, please try again.");
            return "redirect:/vehicles/" + vehicleId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles/" + vehicleId;
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());
            bookingService.cancelBookingCustomer(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }
}
