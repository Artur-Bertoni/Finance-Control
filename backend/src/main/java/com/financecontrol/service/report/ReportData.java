package com.financecontrol.service.report;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public record ReportData(
    LocalDate startDate,
    LocalDate endDate,
    String accountName,
    double totalIncome,
    double totalExpenses,
    double netResult,
    double balance,
    List<DashboardResponse.MonthlyDataPoint> monthly,
    List<DashboardResponse.CategoryDataPoint> expensesByCategory,
    List<DashboardResponse.CategoryDataPoint> incomeByCategory,
    List<TxRow> transactions
) {
    public record TxRow(
        LocalDate date,
        String category,
        String location,
        String description,
        TransactionType type,
        double value
    ) {}
}
