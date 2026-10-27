package com.financetracker.pro.controller;

import com.financetracker.pro.document.SavingsGoal;
import com.financetracker.pro.security.UserPrincipal;
import com.financetracker.pro.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Savings Goals", description = "Financial Target Planning & Contribution APIs")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<SavingsGoal> createGoal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SavingsGoal goal) {
        return new ResponseEntity<>(savingsGoalService.createGoal(userPrincipal.getId(), goal), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all savings goals for authenticated user")
    public ResponseEntity<List<SavingsGoal>> getGoals(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(savingsGoalService.getGoals(userPrincipal.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update savings goal")
    public ResponseEntity<SavingsGoal> updateGoal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody SavingsGoal goal) {
        return ResponseEntity.ok(savingsGoalService.updateGoal(userPrincipal.getId(), id, goal));
    }

    @PostMapping("/{id}/contribute")
    @Operation(summary = "Contribute amount towards goal")
    public ResponseEntity<SavingsGoal> contribute(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> payload) {
        BigDecimal amount = payload.get("amount");
        return ResponseEntity.ok(savingsGoalService.contributeToGoal(userPrincipal.getId(), id, amount));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete savings goal")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        savingsGoalService.deleteGoal(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Savings goal deleted successfully"));
    }
}
