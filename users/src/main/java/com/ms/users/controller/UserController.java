package com.ms.users.controller;

// UserController.java
import com.ms.users.dto.LoginRequest;
import com.ms.users.dto.LoginResponse;
import com.ms.users.dto.UserRegistrationRequest;
import com.ms.users.dto.UserResponse;
import com.ms.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Value("${keycloak.realm}")
    private String realm;

    private final UserService userService;
    private final Keycloak keycloakAdmin;

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-keycloak")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> testKeycloak() {
        try {
            // Try to list users as a basic connectivity test
            List<UserRepresentation> users = keycloakAdmin.realm(realm).users().list();
            return ResponseEntity.ok("Keycloak connection successful. Found " + users.size() + " users");
        } catch (Exception e) {
            log.error("Keycloak connection test failed", e);
            return ResponseEntity.status(500)
                    .body("Keycloak connection failed: " + e.getMessage());
        }
    }
}
