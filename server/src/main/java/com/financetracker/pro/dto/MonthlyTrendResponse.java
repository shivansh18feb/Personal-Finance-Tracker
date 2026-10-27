package com.financetracker.pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendResponse {
    private String monthName;
    private int month;
    private int year;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
}
