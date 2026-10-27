package com.financetracker.pro.dto;

import com.financetracker.pro.document.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String id;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private String category;
    private String description;
    private LocalDate transactionDate;
    private List<String> tags;
    private String merchant;
    private String paymentMethod;
    private String receiptUrl;
    private String recurringRuleId;
    private Instant createdAt;
    private Instant updatedAt;
}
