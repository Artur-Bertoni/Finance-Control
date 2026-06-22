package com.financecontrol.service;

import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.Transaction;
import com.financecontrol.entity.TransactionLocale;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.TransactionRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportExportServiceTest {

    @Mock ReportService         reportService;
    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository     accountRepository;

    @InjectMocks ReportExportService service;

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END   = LocalDate.of(2025, 1, 31);

    private void stubData() {
        DashboardResponse dashboard = new DashboardResponse(
                List.of(new DashboardResponse.MonthlyDataPoint("2025-01", 4000.0, 1500.0)),
                List.of(new DashboardResponse.CategoryDataPoint(1L, "Alimentação", null, 1500.0)),
                List.of(new DashboardResponse.CategoryDataPoint(2L, "Salário", null, 4000.0)),
                List.of(new DashboardResponse.WealthDataPoint("2025-01", 2500.0)));
        when(reportService.getDashboard(eq(1L), any(), any(), any())).thenReturn(dashboard);
        when(accountRepository.sumBalance(eq(1L), any())).thenReturn(2500.0);

        Category cat = new Category();
        cat.setName("Alimentação");
        TransactionLocale loc = new TransactionLocale();
        loc.setName("Mercado");

        Transaction tx = new Transaction();
        tx.setDate(LocalDate.of(2025, 1, 10));
        tx.setCategory(cat);
        tx.setTransactionLocale(loc);
        tx.setObs("Compra do mês");
        tx.setType(TransactionType.DEBIT);
        tx.setValue(150.50);

        when(transactionRepository.findAllFiltered(eq(1L), any(), any(), isNull(), any()))
                .thenReturn(List.of(tx));
    }

    @Test
    void exportPdf_geraBytesNaoVazios() {
        stubData();
        byte[] pdf = service.exportPdf(1L, START, END, null, "pt");
        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void exportExcel_preencheTemplateComAbasELogo() throws Exception {
        stubData();
        byte[] xlsx = service.exportExcel(1L, START, END, null, "pt");
        assertThat(xlsx).isNotEmpty();

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            // Resumo, Por mês, Por categoria, Transações (a aba oculta "_estilos" é removida).
            assertThat(wb.getNumberOfSheets()).isEqualTo(4);
            assertThat(wb.getSheet("_estilos")).isNull();
            assertThat(wb.getSheetAt(3).getRow(0).getCell(0).getStringCellValue()).isNotBlank();
            // logo embutido vindo do template
            assertThat(wb.getAllPictures()).isNotEmpty();
        }
    }

    @Test
    void exportExcel_comIdiomaIngles_naoFalha() {
        stubData();
        assertThat(service.exportExcel(1L, START, END, null, "en")).isNotEmpty();
    }
}
