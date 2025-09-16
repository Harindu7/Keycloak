package com.example.Keycloak.service.impl;

import com.example.Keycloak.model.entity.Organization;
import com.example.Keycloak.model.dto.OrganizationSetupDTO;
import com.example.Keycloak.repository.OrganizationRepository;
import com.example.Keycloak.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Override
    public Organization createOrganization(OrganizationSetupDTO organizationSetupDTO) {
        Organization organization = Organization.builder()
                .name(organizationSetupDTO.getName())
                .description(organizationSetupDTO.getDescription())
                .industry(organizationSetupDTO.getIndustry())
                .website(organizationSetupDTO.getWebsite())
                .address(organizationSetupDTO.getAddress())
                .city(organizationSetupDTO.getCity())
                .state(organizationSetupDTO.getState())
                .country(organizationSetupDTO.getCountry())
                .zipCode(organizationSetupDTO.getZipCode())
                .phone(organizationSetupDTO.getPhone())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return organizationRepository.save(organization);
    }

    @Override
    public Organization findById(String id) {
        return organizationRepository.findById(id).orElse(null);
    }

    @Override
    public Organization updateOrganization(String id, OrganizationSetupDTO organizationSetupDTO) {
        Organization organization = findById(id);
        if (organization != null) {
            organization.setName(organizationSetupDTO.getName());
            organization.setDescription(organizationSetupDTO.getDescription());
            organization.setIndustry(organizationSetupDTO.getIndustry());
            organization.setWebsite(organizationSetupDTO.getWebsite());
            organization.setAddress(organizationSetupDTO.getAddress());
            organization.setCity(organizationSetupDTO.getCity());
            organization.setState(organizationSetupDTO.getState());
            organization.setCountry(organizationSetupDTO.getCountry());
            organization.setZipCode(organizationSetupDTO.getZipCode());
            organization.setPhone(organizationSetupDTO.getPhone());
            organization.setUpdatedAt(LocalDateTime.now());
            return organizationRepository.save(organization);
        }
        return null;
    }

    @Override
    public void deleteOrganization(String id) {
        organizationRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return organizationRepository.existsByName(name);
    }
}
