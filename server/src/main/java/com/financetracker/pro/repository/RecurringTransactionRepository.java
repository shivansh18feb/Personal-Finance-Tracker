package com.financetracker.pro.repository;

import com.financetracker.pro.document.RecurringTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends MongoRepository<RecurringTransaction, String> {
    List<RecurringTransaction> findByUserId(String userId);
    List<RecurringTransaction> findByUserIdAndIsSubscriptionTrue(String userId);
    List<RecurringTransaction> findByStatusAndNextExecutionDateLessThanEqual(String status, LocalDate date);
    Optional<RecurringTransaction> findByIdAndUserId(String id, String userId);
    void deleteAllByUserId(String userId);
}
