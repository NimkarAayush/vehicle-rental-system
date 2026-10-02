package com.vehiclerental.config;

import com.vehiclerental.entity.Role;
import com.vehiclerental.entity.User;
import com.vehiclerental.entity.Vehicle;
import com.vehiclerental.entity.VehicleStatus;
import com.vehiclerental.entity.VehicleType;
import com.vehiclerental.repository.UserRepository;
import com.vehiclerental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProdDatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (!adminExists) {
            log.info("No admin user found. Creating the initial admin user...");
            String adminEmail = env.getProperty("ADMIN_EMAIL");
            String adminPassword = env.getProperty("ADMIN_PASSWORD");

            if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
                throw new IllegalStateException("ADMIN_EMAIL or ADMIN_PASSWORD environment variables are missing. Cannot create initial admin user.");
            }
            if (adminPassword.length() < 12) {
                throw new IllegalStateException("ADMIN_PASSWORD must be at least 12 characters long.");
            }

            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email(adminEmail.toLowerCase())
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Initial admin user created successfully.");
        }

        String seedVehicles = env.getProperty("APP_SEED_DEMO_VEHICLES", "false");
        if ("true".equalsIgnoreCase(seedVehicles) && vehicleRepository.count() == 0) {
            log.info("Seeding demo vehicles...");
            List<Vehicle> vehicles = Arrays.asList(
                createVehicle("Toyota", "Camry", VehicleType.SEDAN, "TX-1001", "45.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fd?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Honda", "CR-V", VehicleType.SUV, "TX-1002", "60.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1568844293986-8d0400ba4705?auto=format&fit=crop&q=80&w=800"),
                createVehicle("Ford", "Mustang", VehicleType.LUXURY, "TX-1003", "120.00", VehicleStatus.AVAILABLE, "https://images.unsplash.com/photo-1584345604476-8ec5e12e42dd?auto=format&fit=crop&q=80&w=800")
            );
            vehicleRepository.saveAll(vehicles);
            log.info("Demo vehicles seeded.");
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
