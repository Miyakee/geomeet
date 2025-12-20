package com.geomeet.api.infrastructure.config;

import com.geomeet.api.application.usecase.login.UserRepository;
import com.geomeet.api.domain.entity.User;
import com.geomeet.api.domain.service.PasswordEncoder;
import com.geomeet.api.domain.valueobject.Email;
import com.geomeet.api.domain.valueobject.PasswordHash;
import com.geomeet.api.domain.valueobject.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Check if users already exist
            if (userRepository.findByUsername("admin").isEmpty()) {
                logger.info("Initializing default users...");

                // Create admin user using domain factory method
                String adminPasswordHash = passwordEncoder.encode("admin123");
                User admin = User.create(
                    new Username("admin"),
                    new Email("admin@geomeet.com"),
                    new PasswordHash(adminPasswordHash)
                );
                userRepository.save(admin);
                logger.info("Created admin user: admin / admin123");

                // Create test user using domain factory method
                String testPasswordHash = passwordEncoder.encode("test123");
                User testUser = User.create(
                    new Username("testuser"),
                    new Email("test@geomeet.com"),
                    new PasswordHash(testPasswordHash)
                );
                userRepository.save(testUser);
                logger.info("Created test user: testuser / test123");
            }

            // Create tty user if it doesn't exist
            if (userRepository.findByUsername("tty").isEmpty()) {
                String ttyPasswordHash = passwordEncoder.encode("tty123");
                User ttyUser = User.create(
                    new Username("tty"),
                    new Email("tty@geomeet.com"),
                    new PasswordHash(ttyPasswordHash)
                );
                userRepository.save(ttyUser);
                logger.info("Created tty user: tty / tty123");
            } else {
                logger.info("tty user already exists, skipping");
            }

            if (userRepository.findByUsername("admin").isPresent()) {
                logger.info("Users initialization completed");
            }
        };
    }
}

