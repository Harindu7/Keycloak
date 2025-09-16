package com.example.Keycloak.repository;

import com.example.Keycloak.model.entity.UserAccountStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountStatusRepository extends MongoRepository<UserAccountStatus, String> {

    Optional<UserAccountStatus> findByKeycloakUserId(String keycloakUserId);

    Optional<UserAccountStatus> findByEmail(String email);

    boolean existsByKeycloakUserId(String keycloakUserId);

    boolean existsByEmail(String email);
}
