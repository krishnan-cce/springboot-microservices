package com.ms.users.service;

import com.ms.users.dto.LoginRequest;
import com.ms.users.dto.LoginResponse;
import com.ms.users.dto.UserRegistrationRequest;
import com.ms.users.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse registerUser(UserRegistrationRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getUserById(String id);
    UserResponse updateUser(String id, UserRegistrationRequest request);
    void deleteUser(String id);
    List<UserResponse> getAllUsers();
    UserResponse getUserByUsername(String username);
}
