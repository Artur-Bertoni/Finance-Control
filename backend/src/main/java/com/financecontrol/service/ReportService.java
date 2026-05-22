package com.financecontrol.service;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public DashboardResponse getDashboard(Long userId,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          Long accountId) {
        LocalDate today = LocalDate.now();

        List<Object[]> monthlyRows = transactionRepository.findMonthlyTotals(userId, startDate, endDate, accountId);
        List<DashboardResponse.MonthlyDataPoint> monthlyData = buildMonthlyData(monthlyRows, startDate, endDate);

        List<Object[]> categoryRows = transactionRepository.findCategoryTotals(userId, startDate, endDate, accountId);
        List<DashboardResponse.CategoryDataPoint> categoryExpenses = new ArrayList<>();
        List<DashboardResponse.CategoryDataPoint> categoryIncomes = new ArrayList<>();
        buildCategoryData(categoryRows, categoryExpenses, categoryIncomes);

        Double currentBalance = accountRepository.sumBalance(userId, null);
        LocalDate wealthStart = startDate.isAfter(today) ? today.minusMonths(1) : startDate;
        List<Object[]> wealthRows = transactionRepository.findMonthlyTotals(userId, wealthStart, today, null);
        List<DashboardResponse.WealthDataPoint> balanceEvolution = buildBalanceEvolution(wealthRows, wealthStart, today, currentBalance);

        return new DashboardResponse(monthlyData, categoryExpenses, categoryIncomes, balanceEvolution);
    }

    private List<DashboardResponse.MonthlyDataPoint> buildMonthlyData(List<Object[]> rows,
                                                                      LocalDate startDate,
                                                                      LocalDate endDate) {
        Map<String, double[]> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String key = monthKey(row);
            double[] arr = map.computeIfAbsent(key, k -> new double[2]);
            if (extractType(row[2]) == TransactionType.CREDIT) arr[0] += ((Number) row[3]).doubleValue();
            else arr[1] += ((Number) row[3]).doubleValue();
        }

        List<DashboardResponse.MonthlyDataPoint> result = new ArrayList<>();
        LocalDate cursor = startDate.withDayOfMonth(1);
        LocalDate end = endDate.withDayOfMonth(1);

        while (!cursor.isAfter(end)) {
            String key = cursor.format(MONTH_FMT);
            double[] arr = map.getOrDefault(key, new double[2]);
            result.add(new DashboardResponse.MonthlyDataPoint(key, arr[0], arr[1]));
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private void buildCategoryData(List<Object[]> rows,
                                    List<DashboardResponse.CategoryDataPoint> expenses,
                                    List<DashboardResponse.CategoryDataPoint> incomes) {
        Map<Long, String> names = new LinkedHashMap<>();
        Map<Long, String> icons = new LinkedHashMap<>();
        Map<Long, double[]> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long catId = (Long) row[0];
            names.put(catId, (String) row[1]);
            icons.put(catId, (String) row[2]);
            double[] arr = map.computeIfAbsent(catId, k -> new double[2]);

            if (extractType(row[3]) == TransactionType.CREDIT) arr[0] += ((Number) row[4]).doubleValue();
            
            else arr[1] += ((Number) row[4]).doubleValue();
        }

        for (Long catId : map.keySet()) {
            double[] arr = map.get(catId);
            String name = names.get(catId);
            String icon = icons.get(catId);
            if (arr[0] > 0) incomes.add(new DashboardResponse.CategoryDataPoint(catId, name, icon, arr[0]));
            if (arr[1] > 0) expenses.add(new DashboardResponse.CategoryDataPoint(catId, name, icon, arr[1]));
        }
        expenses.sort((a, b) -> Double.compare(b.total(), a.total()));
        incomes.sort((a, b) -> Double.compare(b.total(), a.total()));
    }

    private List<DashboardResponse.WealthDataPoint> buildBalanceEvolution(List<Object[]> rows,
                                                                          LocalDate startDate,
                                                                          LocalDate today,
                                                                          Double currentBalance) {
        Map<String, Double> netByMonth = new HashMap<>();
        for (Object[] row : rows) {
            String key = monthKey(row);
            double delta = extractType(row[2]) == TransactionType.CREDIT
                    ? ((Number) row[3]).doubleValue()
                    : -((Number) row[3]).doubleValue();
            netByMonth.merge(key, delta, (a, b) -> a + b);
        }

        LinkedList<DashboardResponse.WealthDataPoint> points = new LinkedList<>();
        double balance = currentBalance;
        LocalDate cursor = today.withDayOfMonth(1);
        LocalDate start = startDate.withDayOfMonth(1);

        while (!cursor.isBefore(start)) {
            String key = cursor.format(MONTH_FMT);
            points.addFirst(new DashboardResponse.WealthDataPoint(key, balance));
            balance -= netByMonth.getOrDefault(key, 0.0);
            cursor = cursor.minusMonths(1);
        }

        return new ArrayList<>(points);
    }

    private String monthKey(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();
        return String.format("%04d-%02d", year, month);
    }

    private TransactionType extractType(Object obj) {
        if (obj instanceof TransactionType tt) return tt;
        if (obj instanceof Number n) return TransactionType.fromCode(n.intValue());
        throw new IllegalArgumentException("Unexpected TransactionType value: " + obj);
    }
}
