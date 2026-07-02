package com.financecontrol.service;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.entity.Transaction;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.TransactionRepository;
import com.financecontrol.service.report.ExcelReportWriter;
import com.financecontrol.service.report.PdfReportWriter;
import com.financecontrol.service.report.ReportData;
import com.financecontrol.service.report.ReportLabels;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final ReportService reportService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public byte[] exportPdf(Long userId, LocalDate startDate, LocalDate endDate, Long accountId, String lang) {
        return PdfReportWriter.write(gather(userId, startDate, endDate, accountId, lang), lang);
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel(Long userId, LocalDate startDate, LocalDate endDate, Long accountId, String lang) {
        return ExcelReportWriter.write(gather(userId, startDate, endDate, accountId, lang), lang);
    }

    private ReportData gather(Long userId, LocalDate startDate, LocalDate endDate, Long accountId, String lang) {
        DashboardResponse dashboard = reportService.getDashboard(userId, startDate, endDate, accountId);

        double income = dashboard.monthlyData().stream().mapToDouble(DashboardResponse.MonthlyDataPoint::income).sum();
        double expenses = dashboard.monthlyData().stream().mapToDouble(DashboardResponse.MonthlyDataPoint::expenses).sum();
        double balance = accountRepository.sumBalance(userId, accountId);

        String accountName = accountId == null
                ? ReportLabels.get(lang, "allAccounts")
                : accountRepository.findById(accountId).map(a -> a.getName()).orElse(ReportLabels.get(lang, "allAccounts"));

        List<ReportData.TxRow> rows = new ArrayList<>();
        for (Transaction t : transactionRepository.findAllFiltered(userId, startDate, endDate, null, accountId)) {
            rows.add(new ReportData.TxRow(
                    t.getDate(),
                    t.getCategory() != null ? t.getCategory().getName() : "",
                    t.getTransactionLocale() != null ? t.getTransactionLocale().getName() : "",
                    t.getObs() != null ? t.getObs() : "",
                    t.getType(),
                    t.getValue() != null ? t.getValue() : 0.0
            ));
        }

        return new ReportData(
                startDate, endDate, accountName,
                income, expenses, income - expenses, balance,
                dashboard.monthlyData(),
                dashboard.categoryExpenses(),
                dashboard.categoryIncomes(),
                rows
        );
    }
}
