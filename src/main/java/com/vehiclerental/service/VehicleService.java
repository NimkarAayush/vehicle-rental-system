package com.vehiclerental.service;

import com.vehiclerental.dto.VehicleFormDto;
import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.entity.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface VehicleService {
    Page<Vehicle> findAllFiltered(String search, VehicleStatus status, Pageable pageable);
    
    Page<Vehicle> findAvailableFiltered(VehicleType type, String brand, BigDecimal minPrice, BigDecimal maxPrice, java.time.LocalDate startDate, java.time.LocalDate endDate, Pageable pageable);

    Vehicle findById(Long id);
    
    Optional<Vehicle> getOptionalById(Long id);

    Vehicle save(VehicleFormDto vehicleDto);

    Vehicle update(Long id, VehicleFormDto vehicleDto);

    void delete(Long id);

    void updateStatus(Long id, VehicleStatus status);
    
    boolean isRegistrationNumberUnique(String registrationNumber, Long currentId);
}
