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
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "recurring_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransaction {

    @Id
    private String id;

    @Indexed
    private String userId;

    private TransactionType type; // INCOME or EXPENSE

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    private String category;

    private String description;

    private String frequency; // DAILY, WEEKLY, MONTHLY, YEARLY

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate nextExecutionDate;

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, PAUSED, CANCELLED

    @Builder.Default
    private boolean isSubscription = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
