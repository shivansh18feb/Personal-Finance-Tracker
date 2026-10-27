package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.RecurringTransaction;
import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.exception.ResourceNotFoundException;
import com.financetracker.pro.repository.RecurringTransactionRepository;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public RecurringTransaction createRecurring(String userId, RecurringTransaction recurring) {
        recurring.setUserId(userId);
        if (recurring.getStartDate() == null) {
            recurring.setStartDate(LocalDate.now());
        }
        if (recurring.getNextExecutionDate() == null) {
            recurring.setNextExecutionDate(recurring.getStartDate());
        }
        return recurringRepository.save(recurring);
    }

    @Override
    public List<RecurringTransaction> getAllRecurring(String userId) {
        return recurringRepository.findByUserId(userId);
    }

    @Override
    public List<RecurringTransaction> getSubscriptions(String userId) {
        return recurringRepository.findByUserIdAndIsSubscriptionTrue(userId);
    }

    @Override
    public RecurringTransaction updateStatus(String userId, String id, String status) {
        RecurringTransaction item = recurringRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found or access denied"));
        item.setStatus(status);
        return recurringRepository.save(item);
    }

    @Override
    public void deleteRecurring(String userId, String id) {
        RecurringTransaction item = recurringRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found or access denied"));
        recurringRepository.delete(item);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    public void processDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueItems =
                recurringRepository.findByStatusAndNextExecutionDateLessThanEqual("ACTIVE", today);

        log.info("Processing {} recurring transactions due on or before {}", dueItems.size(), today);

        for (RecurringTransaction item : dueItems) {
            try {
                // Post actual transaction
                Transaction transaction = Transaction.builder()
                        .userId(item.getUserId())
                        .type(item.getType())
                        .amount(item.getAmount())
                        .currency(item.getCurrency())
                        .category(item.getCategory())
                        .description(item.getDescription() + " (Auto-Recurring)")
                        .transactionDate(item.getNextExecutionDate())
                        .recurringRuleId(item.getId())
                        .deleted(false)
                        .build();

                transactionRepository.save(transaction);

                // Calculate next execution date
                LocalDate nextDate = calculateNextDate(item.getNextExecutionDate(), item.getFrequency());
                if (item.getEndDate() != null && nextDate.isAfter(item.getEndDate())) {
                    item.setStatus("COMPLETED");
                } else {
                    item.setNextExecutionDate(nextDate);
                }
                recurringRepository.save(item);
            } catch (Exception e) {
                log.error("Failed to process recurring rule id: {}", item.getId(), e);
            }
        }
    }

    private LocalDate calculateNextDate(LocalDate current, String frequency) {
        if (frequency == null) return current.plusMonths(1);
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> current.plusDays(1);
            case "WEEKLY" -> current.plusWeeks(1);
            case "YEARLY" -> current.plusYears(1);
            default -> current.plusMonths(1);
        };
    }
}
