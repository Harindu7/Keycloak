package com.example.Keycloak.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller to receive Keycloak event notifications
 * This will trigger SLF4J logging in your Spring Boot backend when users login/register
 */
@RestController
@RequestMapping("/api/keycloak")
@Slf4j
public class KeycloakEventController {

    @PostMapping("/login")
    public ResponseEntity<String> handleLoginEvent(@RequestBody String eventData) {

        try {
            Thread.sleep(10000); // Wait for 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Timer interrupted during registration event processing", e);
        }
        // Log the login event using SLF4J - this will appear in your Spring Boot logs
        log.info("🔐 KEYCLOAK LOGIN EVENT RECEIVED: {}", eventData);
        log.info("✅ User has successfully logged in via Keycloak");

        // Here you can add your future business logic:
        // - Update last login time in database
        // - Send login notifications
        // - Track user activity
        // - Update user statistics
        // - Trigger other services

        return ResponseEntity.ok("Login event processed successfully");
    }

    @PostMapping("/registration")
    public ResponseEntity<String> handleRegistrationEvent(@RequestBody String eventData) {

        // Log the registration event using SLF4J - this will appear in your Spring Boot logs
        log.info("📝 KEYCLOAK REGISTRATION EVENT RECEIVED: {}", eventData);
        log.info("🎉 New user has registered via Keycloak");


        // Here you can add your future business logic:
        // - Create user profile in your database
        // - Send welcome emails
        // - Set up default user settings
        // - Trigger onboarding workflows
        // - Add to mailing lists

        return ResponseEntity.ok("Registration event processed successfully");
    }
}
