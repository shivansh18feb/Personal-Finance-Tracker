package com.financetracker.pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private double savingsRate;
    private BigDecimal monthlyBudget;
    private BigDecimal remainingBudget;
    private double budgetUsedPercentage;
    private String currency;
    private List<TransactionResponse> recentTransactions;
}
