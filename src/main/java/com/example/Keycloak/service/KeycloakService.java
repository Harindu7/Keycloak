package com.example.Keycloak.service;

import com.example.Keycloak.model.dto.UserRegistrationDTO;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakService {

    String createUser(UserRegistrationDTO userRegistrationDTO);

    void sendVerificationEmail(String userId);

    boolean verifyEmail(String token);

    UserRepresentation getUserById(String userId);

    UserRepresentation getUserByEmail(String email);

    void deleteUser(String userId);
}
