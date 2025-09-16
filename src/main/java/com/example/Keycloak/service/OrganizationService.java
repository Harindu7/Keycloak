package com.example.Keycloak.service;

import com.example.Keycloak.model.entity.Organization;
import com.example.Keycloak.model.dto.OrganizationSetupDTO;

public interface OrganizationService {

    Organization createOrganization(OrganizationSetupDTO organizationSetupDTO);

    Organization findById(String id);

    Organization updateOrganization(String id, OrganizationSetupDTO organizationSetupDTO);

    void deleteOrganization(String id);

    boolean existsByName(String name);
}
