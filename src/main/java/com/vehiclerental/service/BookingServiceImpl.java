package com.vehiclerental.service;

import com.vehiclerental.dto.BookingFormDto;
import com.vehiclerental.entity.Booking;
import com.vehiclerental.entity.BookingStatus;
import com.vehiclerental.entity.User;
import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.exception.BookingConflictException;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final Clock clock;

    private static final int MAX_RENTAL_DAYS = 30;

    @Override
    @Transactional
    public Booking createBooking(Long vehicleId, User user, BookingFormDto dto) {
        // Optimistic force increment to lock the vehicle row
        Vehicle vehicle = vehicleRepository.findByIdWithOptimisticForceIncrement(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        validateDates(dto.getStartDate(), dto.getEndDate());

        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            throw new BookingConflictException("Vehicle is currently in maintenance and cannot be booked.");
        }

        checkOverlap(vehicleId, dto.getStartDate(), dto.getEndDate(), null);

        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
        if (days == 0) days = 1; // Same day return is 1 day minimum charge
        
        BigDecimal totalPrice = vehicle.getPricePerDay().multiply(BigDecimal.valueOf(days));

        Booking booking = Booking.builder()
                .user(user)
                .vehicle(vehicle)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = findById(bookingId);
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be confirmed.");
        }

        // Lock vehicle to re-check overlap safely
        Vehicle vehicle = vehicleRepository.findByIdWithOptimisticForceIncrement(booking.getVehicle().getId())
                .orElseThrow();

        checkOverlap(vehicle.getId(), booking.getStartDate(), booking.getEndDate(), booking.getId());

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking startBooking(Long bookingId) {
        Booking booking = findById(bookingId);
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED bookings can be started.");
        }

        Vehicle vehicle = booking.getVehicle();
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        booking.setStatus(BookingStatus.ACTIVE);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking completeBooking(Long bookingId) {
        Booking booking = findById(bookingId);
        
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE bookings can be completed.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        
        handleVehicleReturn(booking.getVehicle(), booking.getId());
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking cancelBookingAdmin(Long bookingId) {
        return performCancellation(bookingId);
    }

    @Override
    @Transactional
    public Booking cancelBookingCustomer(Long bookingId, User customer) {
        Booking booking = findById(bookingId);
        
        if (!booking.getUser().getId().equals(customer.getId())) {
            throw new IllegalStateException("You can only cancel your own bookings.");
        }
        
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only PENDING or CONFIRMED bookings can be cancelled.");
        }

        LocalDate today = LocalDate.now(clock);
        if (!booking.getStartDate().isAfter(today)) {
            throw new IllegalStateException("Bookings can only be cancelled before the pickup date.");
        }

        return performCancellation(bookingId);
    }

    private Booking performCancellation(Long bookingId) {
        Booking booking = findById(bookingId);
        
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking is already finalized.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        handleVehicleReturn(booking.getVehicle(), booking.getId());
        
        return bookingRepository.save(booking);
    }

    private void handleVehicleReturn(Vehicle vehicle, Long excludeBookingId) {
        if (vehicle.getStatus() != VehicleStatus.MAINTENANCE) {
            // Check if there are any other ACTIVE bookings (e.g. concurrent overlapping rentals shouldn't happen, but just in case)
            boolean otherActive = bookingRepository.existsOtherActiveBooking(vehicle.getId(), excludeBookingId);
            if (!otherActive) {
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle);
            }
        }
    }

    @Override
    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    @Override
    public Page<Booking> findAll(String statusFilter, Pageable pageable) {
        Specification<Booking> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (statusFilter != null && !statusFilter.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), BookingStatus.valueOf(statusFilter)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return bookingRepository.findAll(spec, pageable);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now(clock);
        if (start.isBefore(today)) {
            throw new IllegalArgumentException("Pickup date cannot be in the past.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Return date must be after pickup date.");
        }
        long days = ChronoUnit.DAYS.between(start, end);
        if (days > MAX_RENTAL_DAYS) {
            throw new IllegalArgumentException("Maximum rental duration is " + MAX_RENTAL_DAYS + " days.");
        }
    }

    private void checkOverlap(Long vehicleId, LocalDate start, LocalDate end, Long excludeId) {
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.ACTIVE);
        
        boolean overlaps = bookingRepository.existsOverlappingBooking(vehicleId, start, end, activeStatuses, excludeId);
        
        if (overlaps) {
            // Calculate next available dates to suggest
            LocalDate today = LocalDate.now(clock);
            List<Booking> upcoming = bookingRepository.findUpcomingBookings(vehicleId, activeStatuses, today);
            
            String suggestion = calculateNextAvailableSlot(upcoming, start, end);
            throw new BookingConflictException("The vehicle is already booked for the selected dates. " + suggestion);
        }
    }

    private String calculateNextAvailableSlot(List<Booking> upcoming, LocalDate requestedStart, LocalDate requestedEnd) {
        if (upcoming.isEmpty()) return "";
        
        long duration = ChronoUnit.DAYS.between(requestedStart, requestedEnd);
        if (duration == 0) duration = 1;

        LocalDate slotStart = LocalDate.now(clock);
        
        for (Booking b : upcoming) {
            if (slotStart.plusDays(duration).isBefore(b.getStartDate()) || slotStart.plusDays(duration).isEqual(b.getStartDate())) {
                break;
            }
            slotStart = b.getEndDate().plusDays(1); // Next day after return
        }
        
        return "Next available date for your duration is from " + slotStart + ".";
    }
}
