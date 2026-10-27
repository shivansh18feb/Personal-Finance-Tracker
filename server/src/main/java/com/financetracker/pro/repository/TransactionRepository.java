package com.financetracker.pro.repository;

import com.financetracker.pro.document.Transaction;
import com.financetracker.pro.document.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Optional<Transaction> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    List<Transaction> findByUserIdAndDeletedFalse(String userId);

    List<Transaction> findByUserIdAndTransactionDateBetweenAndDeletedFalse(
            String userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndTypeAndTransactionDateBetweenAndDeletedFalse(
            String userId, TransactionType type, LocalDate startDate, LocalDate endDate);

    Page<Transaction> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    void deleteAllByUserId(String userId);
}
