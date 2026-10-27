package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import com.financetracker.pro.document.User;
import com.financetracker.pro.dto.TransactionRequest;
import com.financetracker.pro.dto.TransactionResponse;
import com.financetracker.pro.exception.ResourceNotFoundException;
import com.financetracker.pro.repository.TransactionRepository;
import com.financetracker.pro.repository.UserRepository;
import com.financetracker.pro.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public TransactionResponse createTransaction(String userId, TransactionRequest request) {
        String currency = request.getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = userRepository.findById(userId)
                    .map(User::getPreferredCurrency)
                    .orElse("USD");
        }

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(currency)
                .category(request.getCategory())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .merchant(request.getMerchant())
                .paymentMethod(request.getPaymentMethod())
                .receiptUrl(request.getReceiptUrl())
                .deleted(false)
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    public TransactionResponse getTransactionById(String userId, String id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or access denied"));
        return mapToResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(String userId, String id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or access denied"));

        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            transaction.setCurrency(request.getCurrency());
        }
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        if (request.getTags() != null) {
            transaction.setTags(request.getTags());
        }
        transaction.setMerchant(request.getMerchant());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setReceiptUrl(request.getReceiptUrl());

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    public void deleteTransaction(String userId, String id) {
        Transaction transaction = transactionRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or access denied"));
        // Soft delete
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }

    @Override
    public Page<TransactionResponse> searchTransactions(
            String userId,
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String search,
            Pageable pageable) {

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("deleted").is(false));

        if (type != null) {
            query.addCriteria(Criteria.where("type").is(type));
        }

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("transactionDate").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            query.addCriteria(Criteria.where("transactionDate").gte(startDate));
        } else if (endDate != null) {
            query.addCriteria(Criteria.where("transactionDate").lte(endDate));
        }

        if (minAmount != null && maxAmount != null) {
            query.addCriteria(Criteria.where("amount").gte(minAmount).lte(maxAmount));
        } else if (minAmount != null) {
            query.addCriteria(Criteria.where("amount").gte(minAmount));
        } else if (maxAmount != null) {
            query.addCriteria(Criteria.where("amount").lte(maxAmount));
        }

        if (search != null && !search.isBlank()) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("description").regex(search, "i"),
                    Criteria.where("merchant").regex(search, "i"),
                    Criteria.where("category").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }

        long total = mongoTemplate.count(query, Transaction.class);
        query.with(pageable);
        List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);

        List<TransactionResponse> content = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<TransactionResponse> getTransactionsForPeriod(String userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetweenAndDeletedFalse(userId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .type(t.getType())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .category(t.getCategory())
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .tags(t.getTags())
                .merchant(t.getMerchant())
                .paymentMethod(t.getPaymentMethod())
                .receiptUrl(t.getReceiptUrl())
                .recurringRuleId(t.getRecurringRuleId())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
