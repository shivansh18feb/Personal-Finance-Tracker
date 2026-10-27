package com.financetracker.pro.service;

import com.financetracker.pro.document.SavingsGoal;

import java.math.BigDecimal;
import java.util.List;

public interface SavingsGoalService {
    SavingsGoal createGoal(String userId, SavingsGoal goal);
    List<SavingsGoal> getGoals(String userId);
    SavingsGoal updateGoal(String userId, String id, SavingsGoal goal);
    SavingsGoal contributeToGoal(String userId, String id, BigDecimal amount);
    void deleteGoal(String userId, String id);
}
