package com.financetracker.pro.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fullName;

    @Builder.Default
    private String preferredCurrency = "USD"; // USD, EUR, INR, GBP, etc.

    @Builder.Default
    private String dateFormat = "yyyy-MM-dd";

    @Builder.Default
    private String theme = "light"; // light, dark, system

    @Builder.Default
    private boolean emailVerified = true; // Set default true for developer convenience, can toggle

    @Builder.Default
    private Set<String> roles = new HashSet<>(Set.of("ROLE_USER"));

    // Notifications preferences
    @Builder.Default
    private boolean budgetAlertsEnabled = true;

    @Builder.Default
    private boolean billRemindersEnabled = true;

    @Builder.Default
    private boolean monthlyReportsEnabled = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
