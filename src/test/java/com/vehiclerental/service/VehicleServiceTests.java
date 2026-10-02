package com.vehiclerental.service;

import com.vehiclerental.dto.VehicleFormDto;
import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.entity.VehicleType;
import com.vehiclerental.exception.VehicleInUseException;
import com.vehiclerental.repository.BookingRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTests {

    @Mock
    private VehicleRepository vehicleRepository;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Test
    public void testDelete_throwsExceptionIfBookingsExist() {
        Long vehicleId = 1L;
        when(bookingRepository.existsByVehicleId(vehicleId)).thenReturn(true);
        
        assertThrows(VehicleInUseException.class, () -> vehicleService.delete(vehicleId));
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    @Test
    public void testDelete_successIfNoBookings() {
        Long vehicleId = 1L;
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setImageUrl("/uploads/test.jpg");
        
        when(bookingRepository.existsByVehicleId(vehicleId)).thenReturn(false);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        
        vehicleService.delete(vehicleId);
        
        verify(vehicleRepository, times(1)).delete(vehicle);
        verify(fileStorageService, times(1)).deleteFile("/uploads/test.jpg");
    }
}
