package com.example.Keycloak.config;

import com.example.Keycloak.service.UserAccountStatusService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserAccountStatusService userAccountStatusService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String keycloakUserId = oidcUser.getSubject();
            String email = oidcUser.getEmail();

            // Check if user account status exists
            var userAccountStatus = userAccountStatusService.findByKeycloakUserId(keycloakUserId);
            if (userAccountStatus == null) {
                // Create user account status for new users
                userAccountStatusService.createUserAccountStatus(keycloakUserId, email);
                response.sendRedirect("/organization-setup");
            } else if (!userAccountStatus.getOrgSetupCompleted()) {
                // Redirect to organization setup if not completed
                response.sendRedirect("/organization-setup");
            } else {
                // Redirect to dashboard if organization setup is completed
                response.sendRedirect("/dashboard");
            }
        } else {
            response.sendRedirect("/");
        }
    }
}
