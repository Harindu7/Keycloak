package com.example.Keycloak.controller;

import com.example.Keycloak.model.dto.UserRegistrationDTO;
import com.example.Keycloak.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final KeycloakService keycloakService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistration", new UserRegistrationDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRegistration") UserRegistrationDTO userRegistrationDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            // Check if user already exists
            if (keycloakService.getUserByEmail(userRegistrationDTO.getEmail()) != null) {
                model.addAttribute("error", "User with this email already exists");
                return "auth/register";
            }

            String userId = keycloakService.createUser(userRegistrationDTO);
            redirectAttributes.addFlashAttribute("success",
                "Registration successful! Please check your email for verification link.");

            log.info("User registered successfully with ID: {}", userId);
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Registration failed: ", e);
            model.addAttribute("error", "Registration failed. Please try again.");
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            boolean verified = keycloakService.verifyEmail(token);
            if (verified) {
                redirectAttributes.addFlashAttribute("success", "Email verified successfully! You can now login.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid or expired verification token.");
            }
        } catch (Exception e) {
            log.error("Email verification failed: ", e);
            redirectAttributes.addFlashAttribute("error", "Email verification failed. Please try again.");
        }

        return "redirect:/login";
    }
}
