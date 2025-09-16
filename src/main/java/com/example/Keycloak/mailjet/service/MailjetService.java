package com.example.Keycloak.mailjet.service;


import com.example.Keycloak.mailjet.model.dto.EmailRequestDTO;
import com.example.Keycloak.mailjet.model.dto.EmailResponseDTO;

public interface MailjetService {
    
    EmailResponseDTO sendEmail(EmailRequestDTO emailRequest);
    
    boolean isConfigured();

    void sendVerificationEmail(String toEmail, String firstName, String verificationLink);
}
