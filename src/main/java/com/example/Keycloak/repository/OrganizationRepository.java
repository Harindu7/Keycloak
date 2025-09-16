package com.example.Keycloak.repository;

import com.example.Keycloak.model.entity.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    boolean existsByName(String name);
}
