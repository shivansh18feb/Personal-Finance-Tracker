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

@Document(collection = "budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "user_cat_month_year_idx", def = "{'userId': 1, 'category': 1, 'month': 1, 'year': 1}", unique = true)
})
public class Budget {

    @Id
    private String id;

    @Indexed
    private String userId;

    // "OVERALL" or specific category name (e.g. "Food", "Rent")
    private String category;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    private int month; // 1 to 12
    private int year;  // e.g. 2026

    @Builder.Default
    private double warningThreshold = 80.0; // percentage trigger (e.g. 80%)

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
