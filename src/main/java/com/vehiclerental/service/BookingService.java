package com.vehiclerental.service;

import com.vehiclerental.dto.BookingFormDto;
import com.vehiclerental.entity.Booking;
import com.vehiclerental.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    Booking createBooking(Long vehicleId, User user, BookingFormDto dto);
    
    // Admin state changes
    Booking confirmBooking(Long bookingId);
    Booking startBooking(Long bookingId);
    Booking completeBooking(Long bookingId);
    Booking cancelBookingAdmin(Long bookingId);

    // Customer cancellation
    Booking cancelBookingCustomer(Long bookingId, User customer);

    Booking findById(Long id);
    Page<Booking> findAll(String statusFilter, Pageable pageable);
}
