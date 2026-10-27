package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.Budget;
import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.document.User;
import com.financetracker.pro.dto.BudgetRequest;
import com.financetracker.pro.dto.BudgetResponse;
import com.financetracker.pro.exception.ResourceNotFoundException;
import com.financetracker.pro.repository.BudgetRepository;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.repository.UserRepository;
import com.financetracker.pro.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public BudgetResponse setBudget(String userId, BudgetRequest request) {
        String currency = request.getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = userRepository.findById(userId)
                    .map(User::getPreferredCurrency)
                    .orElse("USD");
        }

        Optional<Budget> existingOpt = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, request.getCategory(), request.getMonth(), request.getYear());

        Budget budget;
        if (existingOpt.isPresent()) {
            budget = existingOpt.get();
            budget.setAmount(request.getAmount());
            budget.setCurrency(currency);
            budget.setWarningThreshold(request.getWarningThreshold());
        } else {
            budget = Budget.builder()
                    .userId(userId)
                    .category(request.getCategory())
                    .amount(request.getAmount())
                    .currency(currency)
                    .month(request.getMonth())
                    .year(request.getYear())
                    .warningThreshold(request.getWarningThreshold())
                    .build();
        }

        Budget saved = budgetRepository.save(budget);
        return calculateBudgetUtilization(saved);
    }

    @Override
    public List<BudgetResponse> getBudgetsForMonth(String userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        return budgets.stream()
                .map(this::calculateBudgetUtilization)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetResponse getBudgetById(String userId, String id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found or access denied"));
        return calculateBudgetUtilization(budget);
    }

    @Override
    public void deleteBudget(String userId, String id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found or access denied"));
        budgetRepository.delete(budget);
    }

    private BudgetResponse calculateBudgetUtilization(Budget budget) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Transaction> expenses = transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
                budget.getUserId(), TransactionType.EXPENSE, startDate, endDate);

        BigDecimal spentAmount = expenses.stream()
                .filter(t -> budget.getCategory().equalsIgnoreCase("OVERALL") ||
                             t.getCategory().equalsIgnoreCase(budget.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remainingAmount = budgetAmount.subtract(spentAmount);

        double percentageUsed = 0.0;
        if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = spentAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        boolean isExceeded = spentAmount.compareTo(budgetAmount) > 0;
        boolean isWarningTriggered = percentageUsed >= budget.getWarningThreshold();

        return BudgetResponse.builder()
                .id(budget.getId())
                .userId(budget.getUserId())
                .category(budget.getCategory())
                .budgetAmount(budgetAmount)
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .percentageUsed(percentageUsed)
                .isExceeded(isExceeded)
                .isWarningTriggered(isWarningTriggered)
                .warningThreshold(budget.getWarningThreshold())
                .currency(budget.getCurrency())
                .month(budget.getMonth())
                .year(budget.getYear())
                .build();
    }
}
