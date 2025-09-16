package com.example.Keycloak.controller;

import com.example.Keycloak.model.dto.OrganizationSetupDTO;
import com.example.Keycloak.model.entity.Organization;
import com.example.Keycloak.service.OrganizationService;
import com.example.Keycloak.service.UserAccountStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final OrganizationService organizationService;
    private final UserAccountStatusService userAccountStatusService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/organization-setup")
    public String showOrganizationSetup(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String keycloakUserId = oidcUser.getSubject();

            // Check if organization setup is already completed
            if (userAccountStatusService.isOrganizationSetupCompleted(keycloakUserId)) {
                return "redirect:/dashboard";
            }

            model.addAttribute("organizationSetup", new OrganizationSetupDTO());
            model.addAttribute("userName", oidcUser.getGivenName() + " " + oidcUser.getFamilyName());
            return "organization/setup";
        }
        return "redirect:/login";
    }

    @PostMapping("/organization-setup")
    public String setupOrganization(@Valid @ModelAttribute("organizationSetup") OrganizationSetupDTO organizationSetupDTO,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {

        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("userName", oidcUser.getGivenName() + " " + oidcUser.getFamilyName());
            return "organization/setup";
        }

        try {
            String keycloakUserId = oidcUser.getSubject();

            // Check if organization name already exists
            if (organizationService.existsByName(organizationSetupDTO.getName())) {
                model.addAttribute("error", "Organization with this name already exists");
                model.addAttribute("userName", oidcUser.getGivenName() + " " + oidcUser.getFamilyName());
                return "organization/setup";
            }

            // Create organization
            Organization organization = organizationService.createOrganization(organizationSetupDTO);

            // Update user account status
            userAccountStatusService.updateOrganizationSetup(keycloakUserId, organization.getId());

            redirectAttributes.addFlashAttribute("success", "Organization setup completed successfully!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            log.error("Organization setup failed: ", e);
            model.addAttribute("error", "Organization setup failed. Please try again.");
            model.addAttribute("userName", oidcUser.getGivenName() + " " + oidcUser.getFamilyName());
            return "organization/setup";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String keycloakUserId = oidcUser.getSubject();

            // Check if organization setup is completed
            if (!userAccountStatusService.isOrganizationSetupCompleted(keycloakUserId)) {
                return "redirect:/organization-setup";
            }

            var userAccountStatus = userAccountStatusService.findByKeycloakUserId(keycloakUserId);
            if (userAccountStatus != null && userAccountStatus.getOrganizationId() != null) {
                Organization organization = organizationService.findById(userAccountStatus.getOrganizationId());
                model.addAttribute("organization", organization);
            }

            // Add user and account status information to the model
            model.addAttribute("userName", oidcUser.getGivenName() + " " + oidcUser.getFamilyName());
            model.addAttribute("userEmail", oidcUser.getEmail());
            model.addAttribute("userAccountStatus", userAccountStatus);

            return "dashboard/index";
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication
        SecurityContextHolder.clearContext();

        // Build Keycloak logout URL with id_token_hint
        String keycloakLogoutUrl = "http://localhost:8080/realms/spring-boot-realm/protocol/openid-connect/logout";
        String postLogoutRedirectUri = "http://localhost:8081/?logout=true";

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String idToken = oidcUser.getIdToken().getTokenValue();
            keycloakLogoutUrl += "?id_token_hint=" + idToken + "&post_logout_redirect_uri=" + postLogoutRedirectUri;
        } else {
            keycloakLogoutUrl += "?post_logout_redirect_uri=" + postLogoutRedirectUri;
        }

        return "redirect:" + keycloakLogoutUrl;
    }
}
