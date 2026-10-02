package com.vehiclerental.integration;

import com.vehiclerental.dto.BookingFormDto;
import com.vehiclerental.entity.*;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.repository.UserRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingIntegrationTests {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Vehicle testVehicle;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setEmail("testbooker@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("Booker");
        testUser.setRole(Role.CUSTOMER);
        testUser = userRepository.save(testUser);

        testVehicle = new Vehicle();
        testVehicle.setBrand("Tesla");
        testVehicle.setModel("Model 3");
        testVehicle.setRegistrationNumber("EV-1234");
        testVehicle.setPricePerDay(BigDecimal.valueOf(100));
        testVehicle.setType(VehicleType.SEDAN);
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
        testVehicle = vehicleRepository.save(testVehicle);
    }

    @Test
    void testBookingLifecycle_Success() {
        BookingFormDto dto = new BookingFormDto();
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(3));
        
        // Create
        Booking booking = bookingService.createBooking(testVehicle.getId(), testUser, dto);
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        
        // Confirm
        booking = bookingService.confirmBooking(booking.getId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        
        // Start
        booking = bookingService.startBooking(booking.getId());
        assertEquals(BookingStatus.ACTIVE, booking.getStatus());
        
        Vehicle v = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertEquals(VehicleStatus.RENTED, v.getStatus());
        
        // Complete
        booking = bookingService.completeBooking(booking.getId());
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        
        v = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertEquals(VehicleStatus.AVAILABLE, v.getStatus());
    }
}
