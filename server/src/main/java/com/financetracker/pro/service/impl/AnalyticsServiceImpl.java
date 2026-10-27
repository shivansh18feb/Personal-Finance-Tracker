package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.Budget;
import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.document.User;
import com.financetracker.pro.dto.*;
import com.financetracker.pro.repository.BudgetRepository;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.repository.UserRepository;
import com.financetracker.pro.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary(String userId, Integer month, Integer year) {
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        User user = userRepository.findById(userId).orElse(null);
        String currency = (user != null) ? user.getPreferredCurrency() : "USD";

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndDeletedFalse(userId, start, end);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0 && netBalance.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netBalance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // Budget evaluation
        Optional<Budget> overallBudgetOpt = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, "OVERALL", targetMonth, targetYear);

        BigDecimal monthlyBudget = overallBudgetOpt.map(Budget::getAmount).orElse(BigDecimal.ZERO);
        BigDecimal remainingBudget = monthlyBudget.subtract(totalExpenses);

        double budgetUsedPercentage = 0.0;
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPercentage = totalExpenses.divide(monthlyBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // Recent 5 transactions
        List<TransactionResponse> recent = transactionRepository
                .findByUserIdAndDeletedFalse(userId, PageRequest.of(0, 5, Sort.by("transactionDate").descending()))
                .getContent()
                .stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .userId(t.getUserId())
                        .type(t.getType())
                        .amount(t.getAmount())
                        .currency(t.getCurrency())
                        .category(t.getCategory())
                        .description(t.getDescription())
                        .transactionDate(t.getTransactionDate())
                        .merchant(t.getMerchant())
                        .paymentMethod(t.getPaymentMethod())
                        .receiptUrl(t.getReceiptUrl())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .savingsRate(savingsRate)
                .monthlyBudget(monthlyBudget)
                .remainingBudget(remainingBudget)
                .budgetUsedPercentage(budgetUsedPercentage)
                .currency(currency)
                .recentTransactions(recent)
                .build();
    }

    @Override
    public List<CategorySpendingResponse> getCategoryWiseExpenses(String userId, Integer month, Integer year) {
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> expenses = transactionRepository
                .findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
                        userId, TransactionType.EXPENSE, start, end);

        BigDecimal totalExpenses = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        return byCategory.entrySet().stream()
                .map(entry -> {
                    double pct = 0.0;
                    if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                        pct = entry.getValue().divide(totalExpenses, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }
                    return CategorySpendingResponse.builder()
                            .category(entry.getKey())
                            .amount(entry.getValue())
                            .percentage(pct)
                            .build();
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyTrendResponse> getMonthlyTrends(String userId, int year) {
        List<MonthlyTrendResponse> trends = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Transaction> monthTransactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndDeletedFalse(userId, start, end);

            BigDecimal income = monthTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = monthTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(MonthlyTrendResponse.builder()
                    .monthName(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .month(m)
                    .year(year)
                    .income(income)
                    .expense(expense)
                    .net(income.subtract(expense))
                    .build());
        }

        return trends;
    }

    @Override
    public List<DailyTrendResponse> getSpendingTrends(String userId, Integer month, Integer year) {
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> expenses = transactionRepository
                .findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
                        userId, TransactionType.EXPENSE, start, end);

        Map<LocalDate, BigDecimal> dailyMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getTransactionDate,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        List<DailyTrendResponse> trends = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            trends.add(DailyTrendResponse.builder()
                    .date(d)
                    .amount(dailyMap.getOrDefault(d, BigDecimal.ZERO))
                    .build());
        }

        return trends;
    }

    @Override
    public List<Map<String, Object>> getFinancialInsights(String userId) {
        List<Map<String, Object>> insights = new ArrayList<>();

        LocalDate today = LocalDate.now();
        YearMonth currentYm = YearMonth.of(today.getYear(), today.getMonthValue());
        YearMonth prevYm = currentYm.minusMonths(1);

        // Calculate this month & last month expense
        BigDecimal currentExp = transactionRepository
                .findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
                        userId, TransactionType.EXPENSE, currentYm.atDay(1), currentYm.atEndOfMonth())
                .stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal prevExp = transactionRepository
                .findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
                        userId, TransactionType.EXPENSE, prevYm.atDay(1), prevYm.atEndOfMonth())
                .stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (prevExp.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = currentExp.subtract(prevExp);
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                insights.add(Map.of(
                        "title", "Spending Increase Warning",
                        "type", "WARNING",
                        "explanation", "Your total expenses this month are already " + diff + " higher than all of last month.",
                        "action", "Review discretionary categories like Dining or Shopping."
                ));
            } else {
                insights.add(Map.of(
                        "title", "Great Budget Discipline",
                        "type", "SUCCESS",
                        "explanation", "Your spending is on track and trending lower than last month.",
                        "action", "Consider depositing the surplus into a savings goal."
                ));
            }
        }

        // Budget check
        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, "OVERALL", today.getMonthValue(), today.getYear())
                .ifPresent(budget -> {
                    if (currentExp.compareTo(budget.getAmount()) > 0) {
                        insights.add(Map.of(
                                "title", "Overall Monthly Budget Exceeded",
                                "type", "ALERT",
                                "explanation", "You have exceeded your monthly limit of " + budget.getAmount() + " by " + currentExp.subtract(budget.getAmount()) + ".",
                                "action", "Pause non-essential purchases until the start of next month."
                        ));
                    }
                });

        return insights;
    }
}
