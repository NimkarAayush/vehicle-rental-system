package com.vehiclerental.dto;

import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class VehicleFormDto {
    private Long id;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Type is required")
    private VehicleType type;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price must be positive")
    private BigDecimal pricePerDay;

    @NotNull(message = "Status is required")
    private VehicleStatus status;

    private MultipartFile imageFile;
    
    private String currentImageUrl;
}
