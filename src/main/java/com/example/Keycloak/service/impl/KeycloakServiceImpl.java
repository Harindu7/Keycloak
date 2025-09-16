package com.example.Keycloak.service.impl;

import com.example.Keycloak.model.dto.UserRegistrationDTO;
import com.example.Keycloak.service.KeycloakService;
import com.example.Keycloak.mailjet.service.MailjetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloakAdminClient;
    private final MailjetService mailjetService;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${app.email-verification-url}")
    private String emailVerificationUrl;

    @Override
    public String createUser(UserRegistrationDTO userRegistrationDTO) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(userRegistrationDTO.getUsername());
            user.setEmail(userRegistrationDTO.getEmail());
            user.setFirstName(userRegistrationDTO.getFirstName());
            user.setLastName(userRegistrationDTO.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false);

            // Create user
            Response response = usersResource.create(user);
            String userId = extractUserIdFromResponse(response);

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userRegistrationDTO.getPassword());
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);

            // Send verification email
            sendVerificationEmail(userId);

            log.info("User created successfully with ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("Error creating user: ", e);
            throw new RuntimeException("Failed to create user in Keycloak", e);
        }
    }

    @Override
    public void sendVerificationEmail(String userId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            UserRepresentation user = realmResource.users().get(userId).toRepresentation();

            String verificationLink = emailVerificationUrl + "?token=" + generateVerificationToken(userId);

            mailjetService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationLink);

            log.info("Verification email sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending verification email: ", e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public boolean verifyEmail(String token) {
        try {
            String userId = validateVerificationToken(token);
            if (userId != null) {
                RealmResource realmResource = keycloakAdminClient.realm(realm);
                UserRepresentation user = realmResource.users().get(userId).toRepresentation();
                user.setEmailVerified(true);
                realmResource.users().get(userId).update(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying email: ", e);
            return false;
        }
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            return realmResource.users().get(userId).toRepresentation();
        } catch (Exception e) {
            log.error("Error getting user by ID: ", e);
            return null;
        }
    }

    @Override
    public UserRepresentation getUserByEmail(String email) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(email, true);
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            log.error("Error getting user by email: ", e);
            return null;
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realm);
            realmResource.users().get(userId).remove();
        } catch (Exception e) {
            log.error("Error deleting user: ", e);
            throw new RuntimeException("Failed to delete user from Keycloak", e);
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getLocation().getPath();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String generateVerificationToken(String userId) {
        // Simple token generation - in production, use JWT or more secure method
        return java.util.Base64.getEncoder().encodeToString((userId + ":" + System.currentTimeMillis()).getBytes());
    }

    private String validateVerificationToken(String token) {
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length == 2) {
                String userId = parts[0];
                long timestamp = Long.parseLong(parts[1]);
                // Token valid for 24 hours
                if (System.currentTimeMillis() - timestamp < 24 * 60 * 60 * 1000) {
                    return userId;
                }
            }
        } catch (Exception e) {
            log.error("Error validating token: ", e);
        }
        return null;
    }
}
