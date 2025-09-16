package com.example.Keycloak.service.impl;

import com.example.Keycloak.model.entity.UserAccountStatus;
import com.example.Keycloak.repository.UserAccountStatusRepository;
import com.example.Keycloak.service.UserAccountStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountStatusServiceImpl implements UserAccountStatusService {

    private final UserAccountStatusRepository userAccountStatusRepository;

    @Override
    public UserAccountStatus createUserAccountStatus(String keycloakUserId, String email) {
        UserAccountStatus userAccountStatus = UserAccountStatus.builder()
                .keycloakUserId(keycloakUserId)
                .email(email)
                .orgSetupCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userAccountStatusRepository.save(userAccountStatus);
    }

    @Override
    public UserAccountStatus findByKeycloakUserId(String keycloakUserId) {
        return userAccountStatusRepository.findByKeycloakUserId(keycloakUserId)
                .orElse(null);
    }

    @Override
    public UserAccountStatus findByEmail(String email) {
        return userAccountStatusRepository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public UserAccountStatus updateOrganizationSetup(String keycloakUserId, String organizationId) {
        UserAccountStatus userAccountStatus = findByKeycloakUserId(keycloakUserId);
        if (userAccountStatus != null) {
            userAccountStatus.setOrganizationId(organizationId);
            userAccountStatus.setOrgSetupCompleted(true);
            userAccountStatus.setUpdatedAt(LocalDateTime.now());
            return userAccountStatusRepository.save(userAccountStatus);
        }
        return null;
    }

    @Override
    public boolean isOrganizationSetupCompleted(String keycloakUserId) {
        UserAccountStatus userAccountStatus = findByKeycloakUserId(keycloakUserId);
        return userAccountStatus != null && userAccountStatus.getOrgSetupCompleted();
    }
}
