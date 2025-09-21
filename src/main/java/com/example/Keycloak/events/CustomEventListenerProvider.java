package com.example.Keycloak.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Custom Keycloak Event Listener Provider
 * This class handles both user events and admin events from Keycloak
 */
public class CustomEventListenerProvider implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(CustomEventListenerProvider.class);

    // HttpClient instance for sending HTTP requests
    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();

    @Override
    public void onEvent(Event event) {
        logger.infof("User Event Received: %s", event.getType());

        // Handle different event types
        switch (event.getType()) {
            case LOGIN:
                handleUserLogin(event);
                break;
            case LOGOUT:
                handleUserLogout(event);
                break;
            case REGISTER:
                handleUserRegistration(event);
                break;
            case LOGIN_ERROR:
                handleLoginError(event);
                break;
            case UPDATE_PASSWORD:
                handlePasswordUpdate(event);
                break;
            case UPDATE_PROFILE:
                handleProfileUpdate(event);
                break;
            case VERIFY_EMAIL:
                handleEmailVerification(event);
                break;
            case RESET_PASSWORD:
                handlePasswordReset(event);
                break;
            default:
                logger.infof("Unhandled user event type: %s", event.getType());
                break;
        }

        // Log event details
        logEventDetails(event);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        logger.infof("Admin Event Received: %s", adminEvent.getOperationType());

        // Handle different admin operation types
        switch (adminEvent.getOperationType()) {
            case CREATE:
                handleAdminCreate(adminEvent);
                break;
            case UPDATE:
                handleAdminUpdate(adminEvent);
                break;
            case DELETE:
                handleAdminDelete(adminEvent);
                break;
            case ACTION:
                handleAdminAction(adminEvent);
                break;
            default:
                logger.infof("Unhandled admin event type: %s", adminEvent.getOperationType());
                break;
        }

        // Log admin event details
        logAdminEventDetails(adminEvent, includeRepresentation);
    }

    private void handleUserLogin(Event event) {
        logger.infof("=== USER LOGIN EVENT ===");
        logger.infof("User login detected - User ID: %s, Client: %s, IP: %s",
                    event.getUserId(), event.getClientId(), event.getIpAddress());

        // Enhanced logging with additional details
        logger.infof("Login Details - Realm: %s, Session: %s, Time: %d",
                    event.getRealmId(), event.getSessionId(), event.getTime());

        // Log additional event details if available
        if (event.getDetails() != null && !event.getDetails().isEmpty()) {
            logger.infof("Login Additional Details: %s", event.getDetails().toString());

            // Log specific login details
            if (event.getDetails().containsKey("username")) {
                logger.infof("Login Username: %s", event.getDetails().get("username"));
            }
            if (event.getDetails().containsKey("auth_method")) {
                logger.infof("Authentication Method: %s", event.getDetails().get("auth_method"));
            }
            if (event.getDetails().containsKey("identity_provider")) {
                logger.infof("Identity Provider: %s", event.getDetails().get("identity_provider"));
            }
        }

        // Security logging
        logger.infof("Login Security Info - IP: %s, User Agent: %s",
                    event.getIpAddress(),
                    event.getDetails() != null ? event.getDetails().get("user_agent") : "N/A");

        logger.infof("=== END LOGIN EVENT ===");

        // Send login event to Spring Boot backend
        sendLoginEventToBackend(event);
    }

    private void handleUserLogout(Event event) {
        logger.infof("=== USER LOGOUT EVENT ===");
        logger.infof("User logout detected - User ID: %s, Client: %s",
                    event.getUserId(), event.getClientId());

        logger.infof("Logout Details - Realm: %s, Session: %s, Time: %d",
                    event.getRealmId(), event.getSessionId(), event.getTime());

        // Log logout reason if available
        if (event.getDetails() != null && !event.getDetails().isEmpty()) {
            logger.infof("Logout Additional Details: %s", event.getDetails().toString());

            if (event.getDetails().containsKey("reason")) {
                logger.infof("Logout Reason: %s", event.getDetails().get("reason"));
            }
        }

        logger.infof("=== END LOGOUT EVENT ===");

        // Add your custom logic here
        // For example: cleanup sessions, log activity, etc.
    }

    private void handleUserRegistration(Event event) {
        logger.infof("=== USER REGISTRATION EVENT ===");
        logger.infof("User registration detected - User ID: %s, Client: %s",
                    event.getUserId(), event.getClientId());

        // Enhanced logging with registration details
        logger.infof("Registration Details - Realm: %s, IP: %s, Time: %d",
                    event.getRealmId(), event.getIpAddress(), event.getTime());

        // Log registration specific details
        if (event.getDetails() != null && !event.getDetails().isEmpty()) {
            logger.infof("Registration Additional Details: %s", event.getDetails().toString());

            // Log specific registration details
            if (event.getDetails().containsKey("username")) {
                logger.infof("Registered Username: %s", event.getDetails().get("username"));
            }
            if (event.getDetails().containsKey("email")) {
                logger.infof("Registered Email: %s", event.getDetails().get("email"));
            }
            if (event.getDetails().containsKey("first_name")) {
                logger.infof("Registered First Name: %s", event.getDetails().get("first_name"));
            }
            if (event.getDetails().containsKey("last_name")) {
                logger.infof("Registered Last Name: %s", event.getDetails().get("last_name"));
            }
            if (event.getDetails().containsKey("email_verified")) {
                logger.infof("Email Verified Status: %s", event.getDetails().get("email_verified"));
            }
        }

        // Registration source tracking
        logger.infof("Registration Source - Client: %s, IP: %s",
                    event.getClientId(), event.getIpAddress());

        logger.infof("=== END REGISTRATION EVENT ===");

        // Send registration event to Spring Boot backend
        sendRegistrationEventToBackend(event);
    }

    private void handleLoginError(Event event) {
        logger.warnf("Login error detected - User ID: %s, Client: %s, IP: %s, Error: %s",
                    event.getUserId(), event.getClientId(), event.getIpAddress(), event.getError());

        // Add your custom logic here
        // For example: security monitoring, account lockout, etc.
    }

    private void handlePasswordUpdate(Event event) {
        logger.infof("Password update detected - User ID: %s", event.getUserId());

        // Add your custom logic here
        // For example: notify user, log security event, etc.
    }

    private void handleProfileUpdate(Event event) {
        logger.infof("Profile update detected - User ID: %s", event.getUserId());

        // Add your custom logic here
        // For example: sync with external systems, validate data, etc.
    }

    private void handleEmailVerification(Event event) {
        logger.infof("Email verification detected - User ID: %s", event.getUserId());

        // Add your custom logic here
        // For example: activate account, send confirmation, etc.
    }

    private void handlePasswordReset(Event event) {
        logger.infof("Password reset detected - User ID: %s", event.getUserId());

        // Add your custom logic here
        // For example: security logging, notify user, etc.
    }

    private void handleAdminCreate(AdminEvent adminEvent) {
        logger.infof("Admin create operation - Resource: %s, Path: %s",
                    adminEvent.getResourceType(), adminEvent.getResourcePath());

        // Check if this is a user registration event
        if (adminEvent.getResourceType() != null &&
            adminEvent.getResourceType().toString().equals("USER") &&
            adminEvent.getResourcePath() != null &&
            adminEvent.getResourcePath().startsWith("users/")) {

            logger.infof("=== USER REGISTRATION EVENT (Admin Create) ===");
            logger.infof("New user created - Resource Path: %s", adminEvent.getResourcePath());
            logger.infof("Registration Details - Realm: %s, Time: %d",
                        adminEvent.getRealmId(), adminEvent.getTime());

            // Extract user ID from the path (format: users/{userId})
            String userId = adminEvent.getResourcePath().substring("users/".length());
            logger.infof("Registered User ID: %s", userId);

            // Send registration event to Spring Boot backend
            sendRegistrationEventToBackendFromAdmin(adminEvent, userId);

            logger.infof("=== END REGISTRATION EVENT ===");
        }

        // Add your custom logic here
        // For example: audit logging, sync with external systems, etc.
    }

    private void handleAdminUpdate(AdminEvent adminEvent) {
        logger.infof("Admin update operation - Resource: %s, Path: %s",
                    adminEvent.getResourceType(), adminEvent.getResourcePath());

        // Add your custom logic here
        // For example: change tracking, notifications, etc.
    }

    private void handleAdminDelete(AdminEvent adminEvent) {
        logger.infof("Admin delete operation - Resource: %s, Path: %s",
                    adminEvent.getResourceType(), adminEvent.getResourcePath());

        // Add your custom logic here
        // For example: cleanup operations, audit trail, etc.
    }

    private void handleAdminAction(AdminEvent adminEvent) {
        logger.infof("Admin action operation - Resource: %s, Path: %s",
                    adminEvent.getResourceType(), adminEvent.getResourcePath());

        // Add your custom logic here
        // For example: custom action handling, notifications, etc.
    }

    private void logEventDetails(Event event) {
        logger.debugf("Event Details - Type: %s, Realm: %s, Client: %s, User: %s, Session: %s, IP: %s, Time: %d",
                     event.getType(),
                     event.getRealmId(),
                     event.getClientId(),
                     event.getUserId(),
                     event.getSessionId(),
                     event.getIpAddress(),
                     event.getTime());

        if (event.getDetails() != null && !event.getDetails().isEmpty()) {
            logger.debugf("Event Details Map: %s", event.getDetails());
        }
    }

    private void logAdminEventDetails(AdminEvent adminEvent, boolean includeRepresentation) {
        logger.debugf("Admin Event Details - Operation: %s, Resource: %s, Path: %s, Realm: %s, Auth Details: %s, Time: %d",
                     adminEvent.getOperationType(),
                     adminEvent.getResourceType(),
                     adminEvent.getResourcePath(),
                     adminEvent.getRealmId(),
                     adminEvent.getAuthDetails(),
                     adminEvent.getTime());

        if (includeRepresentation && adminEvent.getRepresentation() != null) {
            logger.debugf("Admin Event Representation: %s", adminEvent.getRepresentation());
        }
    }

    private void sendLoginEventToBackend(Event event) {
        try {
            // Prepare login event data
            String jsonPayload = String.format(
                "{\"eventType\":\"LOGIN\",\"userId\":\"%s\",\"username\":\"%s\",\"clientId\":\"%s\",\"ipAddress\":\"%s\",\"timestamp\":%d}",
                event.getUserId(),
                event.getDetails() != null ? event.getDetails().get("username") : "unknown",
                event.getClientId(),
                event.getIpAddress(),
                event.getTime()
            );

            // Try multiple backend URLs for different environments
            String[] backendUrls = {
                "http://host.docker.internal:8081/api/keycloak/login",
                "http://localhost:8081/api/keycloak/login",
                "http://172.17.0.1:8081/api/keycloak/login",  // Docker bridge network gateway
                "http://192.168.1.1:8081/api/keycloak/login"   // Alternative gateway
            };

            // Try each URL synchronously until one works
            for (String backendUrl : backendUrls) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(backendUrl))
                            .timeout(Duration.ofSeconds(20))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .build();

                    // Send synchronously with timeout
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        logger.infof("✅ Login event sent to backend successfully via: %s", backendUrl);
                        return; // Success, exit the method
                    } else {
                        logger.warnf("❌ Failed to send login event to backend via %s, status: %d, response: %s",
                                   backendUrl, response.statusCode(), response.body());
                    }
                } catch (Exception e) {
                    logger.warnf("⚠️ Connection failed for %s: %s", backendUrl, e.getMessage());
                }
            }

            logger.errorf("🚨 All backend URLs failed for login event notification");

        } catch (Exception e) {
            logger.errorf("Exception while sending login event to backend: %s", e.getMessage());
        }
    }

    private void sendRegistrationEventToBackend(Event event) {
        try {
            // Prepare registration event data
            String jsonPayload = String.format(
                "{\"eventType\":\"REGISTRATION\",\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"clientId\":\"%s\",\"ipAddress\":\"%s\",\"timestamp\":%d}",
                event.getUserId(),
                event.getDetails() != null ? event.getDetails().get("username") : "unknown",
                event.getDetails() != null ? event.getDetails().get("email") : "unknown",
                event.getClientId(),
                event.getIpAddress(),
                event.getTime()
            );

            // Try multiple backend URLs for different environments
            String[] backendUrls = {
                "http://host.docker.internal:8081/api/keycloak/registration",
                "http://localhost:8081/api/keycloak/registration",
                "http://172.17.0.1:8081/api/keycloak/registration",  // Docker bridge network gateway
                "http://192.168.1.1:8081/api/keycloak/registration"   // Alternative gateway
            };

            // Try each URL synchronously until one works
            for (String backendUrl : backendUrls) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(backendUrl))
                            .timeout(Duration.ofSeconds(3))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .build();

                    // Send synchronously with timeout
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        logger.infof("✅ Registration event sent to backend successfully via: %s", backendUrl);
                        return; // Success, exit the method
                    } else {
                        logger.warnf("❌ Failed to send registration event to backend via %s, status: %d, response: %s",
                                   backendUrl, response.statusCode(), response.body());
                    }
                } catch (Exception e) {
                    logger.warnf("⚠️ Connection failed for %s: %s", backendUrl, e.getMessage());
                }
            }

            logger.errorf("🚨 All backend URLs failed for registration event notification");

        } catch (Exception e) {
            logger.errorf("Exception while sending registration event to backend: %s", e.getMessage());
        }
    }

    private void sendRegistrationEventToBackendFromAdmin(AdminEvent adminEvent, String userId) {
        try {
            // Prepare registration event data from admin event
            String jsonPayload = String.format(
                "{\"eventType\":\"REGISTRATION\",\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"realmId\":\"%s\",\"timestamp\":%d,\"source\":\"admin_create\"}",
                userId,
                "unknown", // Username not available in admin events
                "unknown", // Email not available in admin events
                adminEvent.getRealmId(),
                adminEvent.getTime()
            );

            // Try multiple backend URLs for different environments
            String[] backendUrls = {
                "http://host.docker.internal:8081/api/keycloak/registration",
                "http://localhost:8081/api/keycloak/registration",
                "http://172.17.0.1:8081/api/keycloak/registration",  // Docker bridge network gateway
                "http://192.168.1.1:8081/api/keycloak/registration"   // Alternative gateway
            };

            // Try each URL synchronously until one works
            for (String backendUrl : backendUrls) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(backendUrl))
                            .timeout(Duration.ofSeconds(3))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                            .build();

                    // Send synchronously with timeout
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        logger.infof("✅ Registration event (Admin) sent to backend successfully via: %s", backendUrl);
                        return; // Success, exit the method
                    } else {
                        logger.warnf("❌ Failed to send registration event (Admin) to backend via %s, status: %d, response: %s",
                                   backendUrl, response.statusCode(), response.body());
                    }
                } catch (Exception e) {
                    logger.warnf("⚠️ Connection failed for %s: %s", backendUrl, e.getMessage());
                }
            }

            logger.errorf("🚨 All backend URLs failed for registration event (Admin) notification");

        } catch (Exception e) {
            logger.errorf("Exception while sending registration event (Admin) to backend: %s", e.getMessage());
        }
    }

    public void close() {
        // Cleanup resources if needed
        logger.info("CustomEventListenerProvider is being closed");
    }
}
