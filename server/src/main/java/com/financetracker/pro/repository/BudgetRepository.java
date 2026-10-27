package com.financetracker.pro.repository;

import com.financetracker.pro.document.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {

    List<Budget> findByUserIdAndMonthAndYear(String userId, int month, int year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(String userId, String category, int month, int year);

    Optional<Budget> findByIdAndUserId(String id, String userId);

    void deleteAllByUserId(String userId);
}
