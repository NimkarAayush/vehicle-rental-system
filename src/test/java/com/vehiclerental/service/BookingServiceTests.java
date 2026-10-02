package com.vehiclerental.service;

import com.vehiclerental.dto.BookingFormDto;
import com.vehiclerental.entity.*;
import com.vehiclerental.exception.BookingConflictException;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User testUser;
    private Vehicle testVehicle;
    private BookingFormDto validFormDto;
    private final LocalDate TODAY = LocalDate.of(2025, 1, 1);

    @BeforeEach
    void setUp() {
        // Set clock to fixed time
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneId.of("Asia/Kolkata"));
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());

        testUser = new User();
        testUser.setId(1L);

        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setPricePerDay(BigDecimal.valueOf(50));
        testVehicle.setStatus(VehicleStatus.AVAILABLE);

        validFormDto = new BookingFormDto();
        validFormDto.setStartDate(TODAY.plusDays(1));
        validFormDto.setEndDate(TODAY.plusDays(3));
    }

    @Test
    void testCreateBooking_Success() {
        when(vehicleRepository.findByIdWithOptimisticForceIncrement(1L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.existsOverlappingBooking(eq(1L), any(LocalDate.class), any(LocalDate.class), anyList(), isNull()))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(1L, testUser, validFormDto);

        assertNotNull(booking);
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(BigDecimal.valueOf(100), booking.getTotalPrice()); // 2 days * 50
    }

    @Test
    void testCreateBooking_Overlap() {
        when(vehicleRepository.findByIdWithOptimisticForceIncrement(1L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.existsOverlappingBooking(eq(1L), any(LocalDate.class), any(LocalDate.class), anyList(), isNull()))
                .thenReturn(true);
        when(bookingRepository.findUpcomingBookings(eq(1L), anyList(), any(LocalDate.class))).thenReturn(Collections.emptyList());

        assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking(1L, testUser, validFormDto);
        });
    }

    @Test
    void testCreateBooking_MaintenanceVehicle() {
        testVehicle.setStatus(VehicleStatus.MAINTENANCE);
        when(vehicleRepository.findByIdWithOptimisticForceIncrement(1L)).thenReturn(Optional.of(testVehicle));

        assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking(1L, testUser, validFormDto);
        });
    }
    
    @Test
    void testCreateBooking_SameDayReturn() {
        when(vehicleRepository.findByIdWithOptimisticForceIncrement(1L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        validFormDto.setStartDate(TODAY.plusDays(1));
        validFormDto.setEndDate(TODAY.plusDays(1)); // 0 days diff -> 1 day minimum

        Booking booking = bookingService.createBooking(1L, testUser, validFormDto);

        assertEquals(BigDecimal.valueOf(50), booking.getTotalPrice()); // 1 day * 50
    }

    @Test
    void testCreateBooking_OverMaxDays() {
        when(vehicleRepository.findByIdWithOptimisticForceIncrement(1L)).thenReturn(Optional.of(testVehicle));

        validFormDto.setStartDate(TODAY.plusDays(1));
        validFormDto.setEndDate(TODAY.plusDays(32)); // > 30 days

        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(1L, testUser, validFormDto);
        });
    }

    @Test
    void testCancelBookingCustomer_Success() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUser(testUser);
        booking.setVehicle(testVehicle);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setStartDate(TODAY.plusDays(2)); // in future

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking cancelled = bookingService.cancelBookingCustomer(10L, testUser);

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testCancelBookingCustomer_WrongUser() {
        Booking booking = new Booking();
        booking.setId(10L);
        User otherUser = new User();
        otherUser.setId(99L);
        booking.setUser(otherUser);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> {
            bookingService.cancelBookingCustomer(10L, testUser);
        });
    }
}
