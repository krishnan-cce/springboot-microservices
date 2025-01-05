package com.ms.users.service.impl;

import com.ms.users.dto.LoginRequest;
import com.ms.users.dto.LoginResponse;
import com.ms.users.dto.UserRegistrationRequest;
import com.ms.users.dto.UserResponse;
import com.ms.users.entity.UserProfile;
import com.ms.users.exception.ResourceNotFoundException;
import com.ms.users.mapper.UserMapper;
import com.ms.users.repository.UserProfileRepository;
import com.ms.users.service.KeycloakService;
import com.ms.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserProfileRepository userProfileRepository;
    private final KeycloakService keycloakService;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        // Validate unique constraints
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user in Keycloak
        String keycloakId = keycloakService.createUser(request);

        // Create user profile in local database
        UserProfile userProfile = new UserProfile();
        userProfile.setKeycloakId(keycloakId);
        userProfile = UserMapper.mapToUserProfile(request, userProfile);
        userProfile = userProfileRepository.save(userProfile);

        return UserMapper.mapToUserResponse(userProfile);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        LoginResponse loginResponse = keycloakService.authenticate(request);

        UserProfile userProfile = userProfileRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        loginResponse.setUser(UserMapper.mapToUserResponse(userProfile));

        return loginResponse;
    }

    @Override
    public UserResponse getUserById(String id) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserMapper.mapToUserResponse(userProfile);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UserRegistrationRequest request) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update in Keycloak first
        keycloakService.updateUser(id, userProfile);

        // Update local database
        UserProfile updatedProfile = UserMapper.mapToUserProfile(request, userProfile);
        updatedProfile = userProfileRepository.save(updatedProfile);

        return UserMapper.mapToUserResponse(updatedProfile);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Delete from Keycloak first
        keycloakService.deleteUser(id);

        // Delete from local database
        userProfileRepository.delete(userProfile);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        return UserMapper.mapToUserResponseList(userProfiles);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserMapper.mapToUserResponse(userProfile);
    }
}