package com.financetracker.pro.service;

import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.dto.TransactionRequest;
import com.financetracker.pro.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(String userId, TransactionRequest request);
    TransactionResponse getTransactionById(String userId, String id);
    TransactionResponse updateTransaction(String userId, String id, TransactionRequest request);
    void deleteTransaction(String userId, String id);
    Page<TransactionResponse> searchTransactions(
            String userId,
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String search,
            Pageable pageable
    );
    List<TransactionResponse> getTransactionsForPeriod(String userId, LocalDate startDate, LocalDate endDate);
}
