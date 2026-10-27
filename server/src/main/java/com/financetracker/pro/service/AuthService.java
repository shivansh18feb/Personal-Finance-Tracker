package com.financetracker.pro.service;

import com.financetracker.pro.dto.AuthResponse;
import com.financetracker.pro.dto.LoginRequest;
import com.financetracker.pro.dto.RegisterRequest;
import com.financetracker.pro.dto.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String userId);
    UserResponse updateProfile(String userId, UserResponse updateRequest);
    void changePassword(String userId, String oldPassword, String newPassword);
    void deleteAccount(String userId);
}
