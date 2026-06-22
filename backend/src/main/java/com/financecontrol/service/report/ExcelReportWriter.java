package com.financecontrol.service.report;

import com.financecontrol.dto.response.DashboardResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExcelReportWriter {

    private static final String TEMPLATE = "/templates/report-template.xlsx";
    private static final String STYLE_SHEET = "_estilos";
    private static final String PT = "pt";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int SH_SUMMARY = 0, SH_MONTHLY = 1, SH_CATEGORIES = 2, SH_TRANSACTIONS = 3;

    private ExcelReportWriter() {}

    public static byte[] write(ReportData data, String lang) {
        try (InputStream in = ExcelReportWriter.class.getResourceAsStream(TEMPLATE)) {
            if (in == null) throw new IllegalStateException("Report template not found: " + TEMPLATE);

            try (XSSFWorkbook wb = new XSSFWorkbook(in); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Map<String, CellStyle> styles = loadStyles(wb);

                if (!PT.equals(lang)) localizeLabels(wb, lang);
                renameTabs(wb, lang);

                fillSummary(wb.getSheetAt(SH_SUMMARY), styles, data);
                fillMonthly(wb.getSheetAt(SH_MONTHLY), styles, data.monthly());
                fillCategories(wb.getSheetAt(SH_CATEGORIES), styles, data);
                fillTransactions(wb.getSheetAt(SH_TRANSACTIONS), styles, data.transactions(), lang);

                wb.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Excel report", e);
        }
    }

    private static Map<String, CellStyle> loadStyles(XSSFWorkbook wb) {
        Map<String, CellStyle> styles = new HashMap<>();
        XSSFSheet est = wb.getSheet(STYLE_SHEET);
        if (est != null) {
            for (Row row : est) {
                Cell c = row.getCell(0);
                if (c != null) styles.put(c.getStringCellValue(), c.getCellStyle());
            }
            wb.removeSheetAt(wb.getSheetIndex(est));
        }
        return styles;
    }

    private static void renameTabs(XSSFWorkbook wb, String lang) {
        wb.setSheetName(SH_SUMMARY, lbl(lang, "summary"));
        wb.setSheetName(SH_MONTHLY, lbl(lang, "tabMonthly"));
        wb.setSheetName(SH_CATEGORIES, lbl(lang, "tabCategories"));
        wb.setSheetName(SH_TRANSACTIONS, lbl(lang, "transactions"));
    }

    private static void localizeLabels(XSSFWorkbook wb, String lang) {
        XSSFSheet resumo = wb.getSheetAt(SH_SUMMARY);
        relabel(resumo, 0, 1, lbl(lang, "title"));
        relabel(resumo, 3, 0, lbl(lang, "period"));
        relabel(resumo, 4, 0, lbl(lang, "account"));
        relabel(resumo, 6, 0, lbl(lang, "totalIncome"));
        relabel(resumo, 7, 0, lbl(lang, "totalExpenses"));
        relabel(resumo, 8, 0, lbl(lang, "netResult"));
        relabel(resumo, 9, 0, lbl(lang, "balance"));

        XSSFSheet mensal = wb.getSheetAt(SH_MONTHLY);
        relabel(mensal, 0, 0, lbl(lang, "month"));
        relabel(mensal, 0, 1, lbl(lang, "income"));
        relabel(mensal, 0, 2, lbl(lang, "expenses"));

        XSSFSheet cat = wb.getSheetAt(SH_CATEGORIES);
        relabel(cat, 0, 0, lbl(lang, "expensesByCategory"));
        relabel(cat, 1, 0, lbl(lang, "category"));
        relabel(cat, 1, 1, lbl(lang, "total"));
        relabel(cat, 0, 3, lbl(lang, "incomeByCategory"));
        relabel(cat, 1, 3, lbl(lang, "category"));
        relabel(cat, 1, 4, lbl(lang, "total"));

        XSSFSheet tx = wb.getSheetAt(SH_TRANSACTIONS);
        String[] heads = {"date", "category", "location", "description", "type", "value"};
        for (int i = 0; i < heads.length; i++) relabel(tx, 0, i, lbl(lang, heads[i]));
    }

    private static void fillSummary(XSSFSheet s, Map<String, CellStyle> st, ReportData d) {
        value(s, 3, 1, d.startDate().format(DATE) + " - " + d.endDate().format(DATE), st.get("label"));
        value(s, 4, 1, d.accountName(), st.get("label"));
        money(s, 6, 1, d.totalIncome(),  st.get("moneyGreen"));
        money(s, 7, 1, d.totalExpenses(), st.get("moneyRed"));
        money(s, 8, 1, d.netResult(),    d.netResult() >= 0 ? st.get("moneyGreen") : st.get("moneyRed"));
        money(s, 9, 1, d.balance(),      st.get("moneyPrimary"));
    }

    private static void fillMonthly(XSSFSheet s, Map<String, CellStyle> st,
                                    List<DashboardResponse.MonthlyDataPoint> monthly) {
        int r = 1;
        for (DashboardResponse.MonthlyDataPoint m : monthly) {
            value(s, r, 0, m.month(), st.get("label"));
            money(s, r, 1, m.income(), st.get("moneyGreen"));
            money(s, r, 2, m.expenses(), st.get("moneyRed"));
            r++;
        }
    }

    private static void fillCategories(XSSFSheet s, Map<String, CellStyle> st, ReportData d) {
        int r = 2;
        for (DashboardResponse.CategoryDataPoint c : d.expensesByCategory()) {
            value(s, r, 0, c.categoryName(), st.get("label"));
            money(s, r, 1, c.total(), st.get("moneyRed"));
            r++;
        }
        r = 2;
        for (DashboardResponse.CategoryDataPoint c : d.incomeByCategory()) {
            value(s, r, 3, c.categoryName(), st.get("label"));
            money(s, r, 4, c.total(), st.get("moneyGreen"));
            r++;
        }
    }

    private static void fillTransactions(XSSFSheet s, Map<String, CellStyle> st,
                                         List<ReportData.TxRow> rows, String lang) {
        int r = 1;
        for (ReportData.TxRow tx : rows) {
            boolean credit = "CREDIT".equals(tx.type().name());
            value(s, r, 0, tx.date().format(DATE), st.get("label"));
            value(s, r, 1, tx.category(), st.get("label"));
            value(s, r, 2, tx.location(), st.get("label"));
            value(s, r, 3, tx.description(), st.get("label"));
            value(s, r, 4, lbl(lang, tx.type().name().toLowerCase()), credit ? st.get("green") : st.get("red"));
            money(s, r, 5, credit ? tx.value() : -tx.value(), credit ? st.get("moneyGreen") : st.get("moneyRed"));
            r++;
        }
    }

    private static void relabel(XSSFSheet s, int r, int c, String text) {
        Row row = s.getRow(r);
        if (row == null) return;
        Cell cell = row.getCell(c);
        if (cell != null) cell.setCellValue(text);
    }

    private static void value(XSSFSheet s, int r, int c, String text, CellStyle style) {
        Cell cell = cellAt(s, r, c);
        cell.setCellValue(text == null ? "" : text);
        if (style != null) cell.setCellStyle(style);
    }

    private static void money(XSSFSheet s, int r, int c, double v, CellStyle style) {
        Cell cell = cellAt(s, r, c);
        cell.setCellValue(v);
        if (style != null) cell.setCellStyle(style);
    }

    private static Cell cellAt(XSSFSheet s, int r, int c) {
        Row row = s.getRow(r);
        if (row == null) row = s.createRow(r);
        Cell cell = row.getCell(c);
        if (cell == null) cell = row.createCell(c);
        return cell;
    }

    private static String lbl(String lang, String key) {
        return ReportLabels.get(lang, key);
    }
}
