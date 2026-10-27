package com.financetracker.pro.controller;

import com.financetracker.pro.dto.CategorySpendingResponse;
import com.financetracker.pro.dto.DailyTrendResponse;
import com.financetracker.pro.dto.DashboardSummaryResponse;
import com.financetracker.pro.dto.MonthlyTrendResponse;
import com.financetracker.pro.security.UserPrincipal;
import com.financetracker.pro.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Analytics", description = "Endpoints for KPIs, Aggregated Charts, and Financial Insights")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/summary")
    @Operation(summary = "Get total income, expense, balance, savings rate, and remaining budget")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(analyticsService.getDashboardSummary(userPrincipal.getId(), month, year));
    }

    @GetMapping("/analytics/expenses-by-category")
    @Operation(summary = "Get aggregated expense breakdown by category for pie/donut chart")
    public ResponseEntity<List<CategorySpendingResponse>> getExpensesByCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(analyticsService.getCategoryWiseExpenses(userPrincipal.getId(), month, year));
    }

    @GetMapping("/analytics/monthly-trends")
    @Operation(summary = "Get 12-month income vs expense trends for bar chart")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrends(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(analyticsService.getMonthlyTrends(userPrincipal.getId(), targetYear));
    }

    @GetMapping("/analytics/spending-trends")
    @Operation(summary = "Get day-by-day spending trend for line chart")
    public ResponseEntity<List<DailyTrendResponse>> getSpendingTrends(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(analyticsService.getSpendingTrends(userPrincipal.getId(), month, year));
    }

    @GetMapping("/analytics/financial-insights")
    @Operation(summary = "Get rule-based smart financial insights")
    public ResponseEntity<List<Map<String, Object>>> getFinancialInsights(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(analyticsService.getFinancialInsights(userPrincipal.getId()));
    }
}
