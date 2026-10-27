package com.financetracker.pro.controller;

import com.financetracker.pro.dto.BudgetRequest;
import com.financetracker.pro.dto.BudgetResponse;
import com.financetracker.pro.security.UserPrincipal;
import com.financetracker.pro.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Monthly & Category Budget Management APIs")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create or update a budget")
    public ResponseEntity<BudgetResponse> setBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BudgetRequest request) {
        return new ResponseEntity<>(budgetService.setBudget(userPrincipal.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get budgets with real spending utilization for a given month/year")
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(budgetService.getBudgetsForMonth(userPrincipal.getId(), targetMonth, targetYear));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific budget by ID")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        return ResponseEntity.ok(budgetService.getBudgetById(userPrincipal.getId(), id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget")
    public ResponseEntity<Map<String, String>> deleteBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        budgetService.deleteBudget(userPrincipal.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Budget deleted successfully"));
    }
}
