package com.example.Keycloak.service;

import com.example.Keycloak.model.entity.UserAccountStatus;
import com.example.Keycloak.model.dto.OrganizationSetupDTO;

public interface UserAccountStatusService {

    UserAccountStatus createUserAccountStatus(String keycloakUserId, String email);

    UserAccountStatus findByKeycloakUserId(String keycloakUserId);

    UserAccountStatus findByEmail(String email);

    UserAccountStatus updateOrganizationSetup(String keycloakUserId, String organizationId);

    boolean isOrganizationSetupCompleted(String keycloakUserId);
}
