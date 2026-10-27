package com.financetracker.pro.service;

import com.financetracker.pro.dto.CategorySpendingResponse;
import com.financetracker.pro.dto.DailyTrendResponse;
import com.financetracker.pro.dto.DashboardSummaryResponse;
import com.financetracker.pro.dto.MonthlyTrendResponse;

import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    DashboardSummaryResponse getDashboardSummary(String userId, Integer month, Integer year);
    List<CategorySpendingResponse> getCategoryWiseExpenses(String userId, Integer month, Integer year);
    List<MonthlyTrendResponse> getMonthlyTrends(String userId, int year);
    List<DailyTrendResponse> getSpendingTrends(String userId, Integer month, Integer year);
    List<Map<String, Object>> getFinancialInsights(String userId);
}
