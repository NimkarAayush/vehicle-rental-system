package com.vehiclerental.config;

import com.vehiclerental.entity.*;
import com.vehiclerental.repository.UserRepository;
import com.vehiclerental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("admin@vehiclerental.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
                    
            User customer = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CUSTOMER)
                    .build();
                    
            userRepository.saveAll(Arrays.asList(admin, customer));
        }

        if (vehicleRepository.count() == 0) {
            List<Vehicle> vehicles = Arrays.asList(
                createVehicle("Toyota", "Camry", VehicleType.SEDAN, "TX-1001", "45.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fd?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Honda", "CR-V", VehicleType.SUV, "TX-1002", "60.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1568844293986-8d0400ba4705?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Ford", "Mustang", VehicleType.LUXURY, "TX-1003", "120.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1584345604476-8ec5e12e42dd?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Volkswagen", "Golf", VehicleType.HATCHBACK, "TX-1004", "35.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Mercedes-Benz", "S-Class", VehicleType.LUXURY, "TX-1005", "150.00", VehicleStatus.RENTED, "https://images.unsplash.com/photo-1617531653332-bd46c24f2068?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Tesla", "Model 3", VehicleType.SEDAN, "TX-1006", "80.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Toyota", "Sienna", VehicleType.VAN, "TX-1007", "75.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1608620882875-9e6ab4f0c436?auto=format&fit=crop&q=80&w=800"),
                createVehicle("BMW", "X5", VehicleType.SUV, "TX-1008", "110.00", VehicleStatus.MAINTENANCE, "https://images.unsplash.com/photo-1555008872-f03b347ffb53?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Hyundai", "Elantra", VehicleType.SEDAN, "TX-1009", "40.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1580274455191-1c62238fa333?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Audi", "A4", VehicleType.LUXURY, "TX-1010", "95.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1606152421802-db97b9c7a11b?auto=format&fit=crop&q=80&w=800")
            );
            vehicleRepository.saveAll(vehicles);
        }
    }

    private Vehicle createVehicle(String brand, String model, VehicleType type, String regNum, String price, VehicleStatus status, String img) {
        return Vehicle.builder()
                .brand(brand)
                .model(model)
                .type(type)
                .registrationNumber(regNum)
                .pricePerDay(new BigDecimal(price))
                .status(status)
                .imageUrl(img)
                .build();
    }
}
