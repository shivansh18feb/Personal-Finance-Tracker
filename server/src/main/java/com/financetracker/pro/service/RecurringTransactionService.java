package com.financetracker.pro.service;

import com.financetracker.pro.document.RecurringTransaction;

import java.util.List;

public interface RecurringTransactionService {
    RecurringTransaction createRecurring(String userId, RecurringTransaction recurring);
    List<RecurringTransaction> getAllRecurring(String userId);
    List<RecurringTransaction> getSubscriptions(String userId);
    RecurringTransaction updateStatus(String userId, String id, String status);
    void deleteRecurring(String userId, String id);
    void processDueRecurringTransactions();
}
