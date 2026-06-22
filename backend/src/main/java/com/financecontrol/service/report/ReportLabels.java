package com.financecontrol.service.report;

import java.util.Map;

public final class ReportLabels {

    private static final Map<String, Map<String, String>> L = Map.of(
        "pt", Map.ofEntries(
            Map.entry("title", "Relatório Financeiro"),
            Map.entry("period", "Período"),
            Map.entry("account", "Conta"),
            Map.entry("allAccounts", "Todas as contas"),
            Map.entry("generatedAt", "Gerado em"),
            Map.entry("summary", "Resumo"),
            Map.entry("totalIncome", "Total de Entradas"),
            Map.entry("totalExpenses", "Total de Saídas"),
            Map.entry("netResult", "Resultado"),
            Map.entry("balance", "Saldo em conta(s)"),
            Map.entry("month", "Mês"),
            Map.entry("income", "Entradas"),
            Map.entry("expenses", "Saídas"),
            Map.entry("monthlySection", "Entradas e Saídas por mês"),
            Map.entry("expensesByCategory", "Saídas por categoria"),
            Map.entry("incomeByCategory", "Entradas por categoria"),
            Map.entry("category", "Categoria"),
            Map.entry("total", "Total"),
            Map.entry("transactions", "Transações"),
            Map.entry("date", "Data"),
            Map.entry("location", "Local"),
            Map.entry("description", "Descrição"),
            Map.entry("type", "Tipo"),
            Map.entry("value", "Valor"),
            Map.entry("credit", "Entrada"),
            Map.entry("debit", "Saída"),
            Map.entry("noData", "Sem dados no período"),
            Map.entry("tabMonthly", "Por mês"),
            Map.entry("tabCategories", "Por categoria")
        ),
        "en", Map.ofEntries(
            Map.entry("title", "Financial Report"),
            Map.entry("period", "Period"),
            Map.entry("account", "Account"),
            Map.entry("allAccounts", "All accounts"),
            Map.entry("generatedAt", "Generated at"),
            Map.entry("summary", "Summary"),
            Map.entry("totalIncome", "Total Income"),
            Map.entry("totalExpenses", "Total Expenses"),
            Map.entry("netResult", "Net Result"),
            Map.entry("balance", "Account balance(s)"),
            Map.entry("month", "Month"),
            Map.entry("income", "Income"),
            Map.entry("expenses", "Expenses"),
            Map.entry("monthlySection", "Income and Expenses by month"),
            Map.entry("expensesByCategory", "Expenses by category"),
            Map.entry("incomeByCategory", "Income by category"),
            Map.entry("category", "Category"),
            Map.entry("total", "Total"),
            Map.entry("transactions", "Transactions"),
            Map.entry("date", "Date"),
            Map.entry("location", "Location"),
            Map.entry("description", "Description"),
            Map.entry("type", "Type"),
            Map.entry("value", "Value"),
            Map.entry("credit", "Income"),
            Map.entry("debit", "Expense"),
            Map.entry("noData", "No data in period"),
            Map.entry("tabMonthly", "Monthly"),
            Map.entry("tabCategories", "By category")
        ),
        "es", Map.ofEntries(
            Map.entry("title", "Informe Financiero"),
            Map.entry("period", "Período"),
            Map.entry("account", "Cuenta"),
            Map.entry("allAccounts", "Todas las cuentas"),
            Map.entry("generatedAt", "Generado el"),
            Map.entry("summary", "Resumen"),
            Map.entry("totalIncome", "Total de Ingresos"),
            Map.entry("totalExpenses", "Total de Gastos"),
            Map.entry("netResult", "Resultado"),
            Map.entry("balance", "Saldo en cuenta(s)"),
            Map.entry("month", "Mes"),
            Map.entry("income", "Ingresos"),
            Map.entry("expenses", "Gastos"),
            Map.entry("monthlySection", "Ingresos y Gastos por mes"),
            Map.entry("expensesByCategory", "Gastos por categoría"),
            Map.entry("incomeByCategory", "Ingresos por categoría"),
            Map.entry("category", "Categoría"),
            Map.entry("total", "Total"),
            Map.entry("transactions", "Transacciones"),
            Map.entry("date", "Fecha"),
            Map.entry("location", "Lugar"),
            Map.entry("description", "Descripción"),
            Map.entry("type", "Tipo"),
            Map.entry("value", "Valor"),
            Map.entry("credit", "Ingreso"),
            Map.entry("debit", "Gasto"),
            Map.entry("noData", "Sin datos en el período"),
            Map.entry("tabMonthly", "Por mes"),
            Map.entry("tabCategories", "Por categoría")
        )
    );

    private ReportLabels() {}

    public static String get(String lang, String key) {
        Map<String, String> map = L.getOrDefault(lang == null ? "pt" : lang, L.get("pt"));
        return map.getOrDefault(key, L.get("pt").getOrDefault(key, key));
    }
}
