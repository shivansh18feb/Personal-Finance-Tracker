package com.financetracker.pro.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'transactionDate': -1}"),
    @CompoundIndex(name = "user_type_date_idx", def = "{'userId': 1, 'type': 1, 'transactionDate': -1}"),
    @CompoundIndex(name = "user_category_date_idx", def = "{'userId': 1, 'category': 1, 'transactionDate': -1}")
})
public class Transaction {

    @Id
    private String id;

    @Indexed
    private String userId;

    private TransactionType type;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    @Indexed
    private String category;

    private String description;

    @Indexed
    private LocalDate transactionDate;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private String merchant;

    private String paymentMethod; // e.g. "Credit Card", "Bank Transfer", "Cash"

    private String receiptUrl;

    private String recurringRuleId; // If generated from recurring transaction

    @Builder.Default
    private boolean deleted = false; // Soft-delete support

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
