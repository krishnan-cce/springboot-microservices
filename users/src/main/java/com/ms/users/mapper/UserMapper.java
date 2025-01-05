package com.ms.users.mapper;

import com.ms.users.dto.UserRegistrationRequest;
import com.ms.users.dto.UserResponse;
import com.ms.users.entity.UserProfile;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class UserMapper {

    public static UserResponse mapToUserResponse(UserProfile userProfile) {
        UserResponse response = new UserResponse();
        response.setKeycloakId(userProfile.getKeycloakId());
        response.setUsername(userProfile.getUsername());
        response.setEmail(userProfile.getEmail());
        response.setFirstName(userProfile.getFirstName());
        response.setLastName(userProfile.getLastName());
        response.setPhoneNumber(userProfile.getPhoneNumber());
        response.setRoles(userProfile.getRoles());
        response.setCreatedAt(userProfile.getCreatedAt());
        return response;
    }

    public static UserProfile mapToUserProfile(UserRegistrationRequest request, UserProfile userProfile) {
        userProfile.setUsername(request.getUsername());
        userProfile.setEmail(request.getEmail());
        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());
        userProfile.setPhoneNumber(request.getPhoneNumber());
        userProfile.setRoles(request.getRoles() != null ? request.getRoles() : new HashSet<>());
        return userProfile;
    }

    public static List<UserResponse> mapToUserResponseList(List<UserProfile> userProfiles) {
        return userProfiles.stream()
                .map(UserMapper::mapToUserResponse)
                .collect(Collectors.toList());
    }
}