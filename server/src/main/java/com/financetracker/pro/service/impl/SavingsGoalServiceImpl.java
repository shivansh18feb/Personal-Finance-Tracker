package com.financetracker.pro.service.impl;

import com.financetracker.pro.document.SavingsGoal;
import com.financetracker.pro.exception.ResourceNotFoundException;
import com.financetracker.pro.repository.SavingsGoalRepository;
import com.financetracker.pro.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    @Override
    public SavingsGoal createGoal(String userId, SavingsGoal goal) {
        goal.setUserId(userId);
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        goal.setStatus("IN_PROGRESS");
        return savingsGoalRepository.save(goal);
    }

    @Override
    public List<SavingsGoal> getGoals(String userId) {
        return savingsGoalRepository.findByUserId(userId);
    }

    @Override
    public SavingsGoal updateGoal(String userId, String id, SavingsGoal updated) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found or access denied"));

        goal.setName(updated.getName());
        goal.setDescription(updated.getDescription());
        goal.setTargetAmount(updated.getTargetAmount());
        goal.setTargetDate(updated.getTargetDate());
        if (updated.getStatus() != null) {
            goal.setStatus(updated.getStatus());
        }

        return savingsGoalRepository.save(goal);
    }

    @Override
    public SavingsGoal contributeToGoal(String userId, String id, BigDecimal amount) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found or access denied"));

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);

        if (goal.getTargetAmount() != null && newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");
        }

        return savingsGoalRepository.save(goal);
    }

    @Override
    public void deleteGoal(String userId, String id) {
        SavingsGoal goal = savingsGoalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found or access denied"));
        savingsGoalRepository.delete(goal);
    }
}
