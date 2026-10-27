package com.financetracker.pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String preferredCurrency;
    private String dateFormat;
    private String theme;
    private boolean emailVerified;
    private boolean budgetAlertsEnabled;
    private boolean billRemindersEnabled;
    private boolean monthlyReportsEnabled;
    private Set<String> roles;
}
