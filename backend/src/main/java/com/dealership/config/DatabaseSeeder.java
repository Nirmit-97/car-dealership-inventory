package com.dealership.config;

import com.dealership.user.Role;
import com.dealership.user.User;
import com.dealership.user.UserRepository;
import com.dealership.vehicle.Vehicle;
import com.dealership.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with initial data (Users and Vehicles) when the app starts.
 *
 * Implements CommandLineRunner to run automatically after Spring context is loaded.
 * Ensures the database is not empty so the frontend has data immediately.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking seeded database users...");
        ensureUserExists("admin@dealer.com", "password", Role.ADMIN);
        ensureUserExists("user@dealer.com", "password", Role.USER);

        if (vehicleRepository.count() == 0) {
            log.info("Vehicle inventory is empty. Seeding demo vehicles...");
            seedVehicles();
            log.info("Database vehicle seeding completed.");
        } else {
            log.info("Vehicle inventory contains data. Vehicle seeding skipped.");
        }
    }

    private void ensureUserExists(String email, String rawPassword, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                if (user.getRole() != role) {
                    user.setRole(role);
                    userRepository.save(user);
                    log.info("Updated existing user {} to role {}", email, role);
                }
            },
            () -> {
                User newUser = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .build();
                userRepository.save(newUser);
                log.info("Seeded new user Account {} with role {}", email, role);
            }
        );
    }

    private void seedVehicles() {
        List<Vehicle> demoVehicles = List.of(
                buildVehicle("Toyota", "Camry", "Sedan", 28000.0, 15),
                buildVehicle("Toyota", "RAV4", "SUV", 32000.0, 12),
                buildVehicle("Honda", "Civic", "Sedan", 25000.0, 20),
                buildVehicle("Honda", "CR-V", "SUV", 30000.0, 18),
                buildVehicle("Ford", "Mustang", "Coupe", 45000.0, 5),
                buildVehicle("Ford", "F-150", "Truck", 40000.0, 25),
                buildVehicle("BMW", "3 Series", "Sedan", 48000.0, 8),
                buildVehicle("BMW", "X5", "SUV", 65000.0, 6),
                buildVehicle("Tesla", "Model 3", "Electric", 45000.0, 10),
                buildVehicle("Tesla", "Model Y", "Electric", 52000.0, 14)
        );

        vehicleRepository.saveAll(demoVehicles);
        log.info("Seeded 10 demo vehicles.");
    }

    private Vehicle buildVehicle(String make, String model, String category, Double price, Integer quantity) {
        return Vehicle.builder()
                .make(make)
                .model(model)
                .category(category)
                .price(price)
                .quantity(quantity)
                .build();
    }
}
