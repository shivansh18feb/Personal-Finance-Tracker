package com.financetracker.pro.service;

import com.financetracker.pro.document.Budget;
import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.dto.BudgetRequest;
import com.financetracker.pro.dto.BudgetResponse;
import com.financetracker.pro.repository.BudgetRepository;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.repository.UserRepository;
import com.financetracker.pro.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @Test
    void testSetAndCalculateBudget() {
        BudgetRequest request = BudgetRequest.builder()
                .category("Food")
                .amount(new BigDecimal("500.00"))
                .month(10)
                .year(2026)
                .warningThreshold(80.0)
                .currency("USD")
                .build();

        Budget budget = Budget.builder()
                .id("b-1")
                .userId("u-1")
                .category("Food")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .month(10)
                .year(2026)
                .warningThreshold(80.0)
                .build();

        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear("u-1", "Food", 10, 2026))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Transaction expense = Transaction.builder()
                .category("Food")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("250.00"))
                .build();

        when(transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
                eq("u-1"), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(expense));

        BudgetResponse response = budgetService.setBudget("u-1", request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getBudgetAmount());
        assertEquals(new BigDecimal("250.00"), response.getSpentAmount());
        assertEquals(new BigDecimal("250.00"), response.getRemainingAmount());
        assertEquals(50.0, response.getPercentageUsed());
        assertFalse(response.isExceeded());
        assertFalse(response.isWarningTriggered());
    }
}
