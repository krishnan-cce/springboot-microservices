package com.ms.users.service.impl;

import com.ms.users.dto.LoginRequest;
import com.ms.users.dto.LoginResponse;
import com.ms.users.dto.UserRegistrationRequest;
import com.ms.users.entity.UserProfile;
import com.ms.users.exception.CustomAuthenticationException;
import com.ms.users.exception.UserAlreadyExistsException;
import com.ms.users.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final Keycloak keycloakAdmin;


    @Override
    public String createUser(UserRegistrationRequest request) {
        log.debug("Attempting to create user in Keycloak with username: {}", request.getUsername());

        try {
            // First check if user exists
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<UserRepresentation> existingUsers = realmResource.users()
                    .searchByEmail(request.getEmail(), true);

            if (!existingUsers.isEmpty()) {
                throw new UserAlreadyExistsException("Email already registered");
            }

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false);

            // Set credentials
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // Create user
            Response response = keycloakAdmin.realm(realm).users().create(user);
            log.info("Keycloak create user response status: {}", response.getStatus());

            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException("User with this email or username already exists");
            }

            if (response.getStatus() != 201) {
                String responseBody = response.readEntity(String.class);
                log.error("Failed to create user in Keycloak. Status: {}, Body: {}", response.getStatus(), responseBody);
                throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
            }

            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("Created user with ID: {}", userId);

            // Assign roles if provided
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                assignRoles(userId, request.getRoles());
            }

            return userId;

        } catch (UserAlreadyExistsException e) {
            log.warn("User already exists: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new RuntimeException("Failed to create user in Keycloak", e);
        }
    }

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        try (Keycloak keycloakUser = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(request.getUsername())
                .password(request.getPassword())
                .grantType(OAuth2Constants.PASSWORD)
                .build()) {

            // Get the token
            AccessTokenResponse tokenResponse = keycloakUser.tokenManager().getAccessToken();

            // Find user in Keycloak to get ID
            UserRepresentation userRepresentation = keycloakAdmin.realm(realm)
                    .users()
                    .searchByUsername(request.getUsername(), true)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new CustomAuthenticationException("User not found in Keycloak"));

            return LoginResponse.builder()
                    .accessToken(tokenResponse.getToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .keycloakId(userRepresentation.getId())
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed", e);
            throw new CustomAuthenticationException("Invalid credentials", e);
        }
    }

    @Override
    public void updateUser(String userId, UserProfile userProfile) {
        UserResource userResource = keycloakAdmin.realm(realm).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();

        user.setFirstName(userProfile.getFirstName());
        user.setLastName(userProfile.getLastName());
        user.setEmail(userProfile.getEmail());

        userResource.update(user);
    }

    @Override
    public void deleteUser(String userId) {
        Response response = keycloakAdmin.realm(realm).users().delete(userId);
        if (response.getStatus() != 204) {
            throw new RuntimeException("Failed to delete user in Keycloak");
        }
    }

    @Override
    public void assignRoles(String userId, Set<String> roles) {
        log.debug("Attempting to assign roles: {} to user: {}", roles, userId);
        UserResource userResource = keycloakAdmin.realm(realm).users().get(userId);

        List<RoleRepresentation> roleRepresentations = new ArrayList<>();

        for (String roleName : roles) {
            List<RoleRepresentation> availableRoles = keycloakAdmin.realm(realm).roles().list();
            RoleRepresentation roleRepresentation = availableRoles.stream()
                    .filter(role -> role.getName().equalsIgnoreCase(roleName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            log.debug("Found role: {}", roleName);
            roleRepresentations.add(roleRepresentation);
        }

        try {
            userResource.roles().realmLevel().add(roleRepresentations);
            log.info("Successfully assigned {} roles to user: {}", roleRepresentations.size(), userId);
        } catch (Exception e) {
            log.error("Failed to assign roles to user: {}", userId, e);
            throw new RuntimeException("Failed to assign roles", e);
        }
    }

    private void printRoleDetails(String roleName) {
        try {
            RoleResource roleResource = keycloakAdmin.realm(realm).roles().get(roleName);
            RoleRepresentation roleRepresentation = roleResource.toRepresentation();

            log.debug("Role Details for {}: ", roleName);
            log.debug("ID: {}", roleRepresentation.getId());
            log.debug("Name: {}", roleRepresentation.getName());
            log.debug("Description: {}", roleRepresentation.getDescription());
        } catch (Exception e) {
            log.error("Error retrieving details for role: {}", roleName, e);
        }
    }
}