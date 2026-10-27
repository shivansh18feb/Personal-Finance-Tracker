package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.User;
import com.financetracker.pro.dto.AuthResponse;
import com.financetracker.pro.dto.LoginRequest;
import com.financetracker.pro.dto.RegisterRequest;
import com.financetracker.pro.dto.UserResponse;
import com.financetracker.pro.exception.BadRequestException;
import com.financetracker.pro.exception.ResourceNotFoundException;
import com.financetracker.pro.repository.*;
import com.financetracker.pro.security.JwtTokenProvider;
import com.financetracker.pro.security.UserPrincipal;
import com.financetracker.pro.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .preferredCurrency(request.getPreferredCurrency() != null ? request.getPreferredCurrency() : "USD")
                .build();

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateTokenFromUserId(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateProfile(String userId, UserResponse updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (updateRequest.getFullName() != null) user.setFullName(updateRequest.getFullName());
        if (updateRequest.getPreferredCurrency() != null) user.setPreferredCurrency(updateRequest.getPreferredCurrency());
        if (updateRequest.getDateFormat() != null) user.setDateFormat(updateRequest.getDateFormat());
        if (updateRequest.getTheme() != null) user.setTheme(updateRequest.getTheme());
        user.setBudgetAlertsEnabled(updateRequest.isBudgetAlertsEnabled());
        user.setBillRemindersEnabled(updateRequest.isBillRemindersEnabled());
        user.setMonthlyReportsEnabled(updateRequest.isMonthlyReportsEnabled());

        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String userId) {
        transactionRepository.deleteAllByUserId(userId);
        budgetRepository.deleteAllByUserId(userId);
        savingsGoalRepository.deleteAllByUserId(userId);
        recurringTransactionRepository.deleteAllByUserId(userId);
        notificationRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .preferredCurrency(user.getPreferredCurrency())
                .dateFormat(user.getDateFormat())
                .theme(user.getTheme())
                .emailVerified(user.isEmailVerified())
                .budgetAlertsEnabled(user.isBudgetAlertsEnabled())
                .billRemindersEnabled(user.isBillRemindersEnabled())
                .monthlyReportsEnabled(user.isMonthlyReportsEnabled())
                .roles(user.getRoles())
                .build();
    }
}
