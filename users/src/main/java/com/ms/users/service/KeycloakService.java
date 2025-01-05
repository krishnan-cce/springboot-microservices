package com.ms.users.service;

import com.ms.users.dto.LoginRequest;
import com.ms.users.dto.LoginResponse;
import com.ms.users.dto.UserRegistrationRequest;
import com.ms.users.dto.UserResponse;
import com.ms.users.entity.UserProfile;

import java.util.Set;

public interface KeycloakService {
    String createUser(UserRegistrationRequest request);
    void updateUser(String userId, UserProfile userProfile);
    void deleteUser(String userId);
    LoginResponse authenticate(LoginRequest request);
    void assignRoles(String userId, Set<String> roles);
}
