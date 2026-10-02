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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

@ActiveProfiles("test")
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
        bookingRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("testbooker" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("Booker");
        testUser.setRole(Role.CUSTOMER);
        testUser = userRepository.save(testUser);

        testVehicle = new Vehicle();
        testVehicle.setBrand("Tesla");
        testVehicle.setModel("Model 3");
        testVehicle.setRegistrationNumber("EV-" + System.currentTimeMillis());
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

    @Test
    void testConcurrentBookings_OptimisticLocking() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    BookingFormDto dto = new BookingFormDto();
                    dto.setStartDate(LocalDate.now().plusDays(5));
                    dto.setEndDate(LocalDate.now().plusDays(10));
                    bookingService.createBooking(testVehicle.getId(), testUser, dto);
                    successCount.incrementAndGet();
                } catch (org.springframework.orm.ObjectOptimisticLockingFailureException | com.vehiclerental.exception.BookingConflictException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertEquals(1, successCount.get(), "Only one booking should succeed");
        assertEquals(1, failCount.get(), "One booking should fail due to lock or overlap");
    }

    @Test
    void testOverlapEdges() {
        BookingFormDto dto = new BookingFormDto();
        dto.setStartDate(LocalDate.now().plusDays(2));
        dto.setEndDate(LocalDate.now().plusDays(5));
        bookingService.createBooking(testVehicle.getId(), testUser, dto);

        // Same dates should fail
        assertThrows(com.vehiclerental.exception.BookingConflictException.class, () -> {
            BookingFormDto conflict = new BookingFormDto();
            conflict.setStartDate(LocalDate.now().plusDays(2));
            conflict.setEndDate(LocalDate.now().plusDays(5));
            bookingService.createBooking(testVehicle.getId(), testUser, conflict);
        });

        // Inside range should fail
        assertThrows(com.vehiclerental.exception.BookingConflictException.class, () -> {
            BookingFormDto conflict = new BookingFormDto();
            conflict.setStartDate(LocalDate.now().plusDays(3));
            conflict.setEndDate(LocalDate.now().plusDays(4));
            bookingService.createBooking(testVehicle.getId(), testUser, conflict);
        });

        // Back to back should succeed
        BookingFormDto backToBack = new BookingFormDto();
        backToBack.setStartDate(LocalDate.now().plusDays(5));
        backToBack.setEndDate(LocalDate.now().plusDays(7));
        assertDoesNotThrow(() -> {
            bookingService.createBooking(testVehicle.getId(), testUser, backToBack);
        });
    }
}
