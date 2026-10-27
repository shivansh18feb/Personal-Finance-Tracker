package com.financetracker.pro.service;

import com.financetracker.pro.dto.BudgetRequest;
import com.financetracker.pro.dto.BudgetResponse;

import java.util.List;

public interface BudgetService {
    BudgetResponse setBudget(String userId, BudgetRequest request);
    List<BudgetResponse> getBudgetsForMonth(String userId, int month, int year);
    BudgetResponse getBudgetById(String userId, String id);
    void deleteBudget(String userId, String id);
}
