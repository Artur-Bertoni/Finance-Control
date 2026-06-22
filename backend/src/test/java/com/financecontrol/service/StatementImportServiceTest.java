package com.financecontrol.service;

import com.financecontrol.dto.request.ImportRowRequest;
import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.financecontrol.dto.response.ParsedTransactionResponse;
import com.financecontrol.entity.Category;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatementImportServiceTest {

    @Mock TransactionService transactionService;
    @Mock CategoryService    categoryService;

    @InjectMocks StatementImportService statementImportService;

    // ── previewStatement ─────────────────────────────────────────────────────

    @Test
    void previewStatement_arquivoNaoPdf_lancaUncheckedIOException() {
        MultipartFile file = new MockMultipartFile(
                "file", "extrato.pdf", "application/pdf",
                "not a real pdf content".getBytes());

        assertThatThrownBy(() -> statementImportService.previewStatement(1L, file))
                .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void previewStatement_bytesVazios_lancaUncheckedIOException() {
        MultipartFile file = new MockMultipartFile(
                "file", "extrato.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> statementImportService.previewStatement(1L, file))
                .isInstanceOf(UncheckedIOException.class);
    }

    // ── confirmImport ────────────────────────────────────────────────────────

    @Test
    void confirmImport_listaVazia_retornaResultadoZeradoSemDatas() {
        ImportResult result = statementImportService.confirmImport(1L, 5L, List.of());

        assertThat(result.imported()).isZero();
        assertThat(result.startDate()).isNull();
        assertThat(result.endDate()).isNull();
        verifyNoInteractions(transactionService, categoryService);
    }

    @Test
    void confirmImport_linhasMarcadasSkip_saoIgnoradas() {
        ImportRowRequest skipped = new ImportRowRequest(
                "2025-01-10", "Mercado", 50.0, TransactionType.DEBIT, 3L, null, true);

        ImportResult result = statementImportService.confirmImport(1L, 5L, List.of(skipped));

        assertThat(result.imported()).isZero();
        verify(transactionService, never()).create(any(), any(), anyBoolean());
        verify(categoryService, never()).learnAlias(any(), any(), any());
    }

    @Test
    void confirmImport_linhaSemCategoria_ehIgnorada() {
        ImportRowRequest noCategory = new ImportRowRequest(
                "2025-01-10", "Mercado", 50.0, TransactionType.DEBIT, null, null, false);

        ImportResult result = statementImportService.confirmImport(1L, 5L, List.of(noCategory));

        assertThat(result.imported()).isZero();
        verify(transactionService, never()).create(any(), any(), anyBoolean());
    }

    @Test
    void confirmImport_linhasValidas_criaTransacoesEAprendeAlias() {
        ImportRowRequest r1 = new ImportRowRequest(
                "2025-01-10", "Mercado", 50.0, TransactionType.DEBIT, 3L, 7L, false);
        ImportRowRequest r2 = new ImportRowRequest(
                "2025-01-20", "Salário", 4000.0, TransactionType.CREDIT, 4L, null, false);

        ImportResult result = statementImportService.confirmImport(1L, 5L, List.of(r1, r2));

        assertThat(result.imported()).isEqualTo(2);
        assertThat(result.startDate()).isEqualTo("2025-01-10");
        assertThat(result.endDate()).isEqualTo("2025-01-20");

        verify(categoryService).learnAlias(1L, "Mercado", 3L);
        verify(categoryService).learnAlias(1L, "Salário", 4L);

        ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
        verify(transactionService, times(2)).create(eq(1L), captor.capture(), eq(true));

        List<TransactionRequest> sent = captor.getAllValues();
        assertThat(sent.get(0).accountId()).isEqualTo(5L);
        assertThat(sent.get(0).categoryId()).isEqualTo(3L);
        assertThat(sent.get(0).transactionLocaleId()).isEqualTo(7L);
        assertThat(sent.get(0).value()).isEqualTo(50.0);
        assertThat(sent.get(0).type()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void confirmImport_misturaSkipEValidas_contaApenasImportadas() {
        ImportRowRequest skipped = new ImportRowRequest(
                "2025-02-01", "Ignorar", 10.0, TransactionType.DEBIT, 3L, null, true);
        ImportRowRequest valid = new ImportRowRequest(
                "2025-02-05", "Conta de luz", 120.0, TransactionType.DEBIT, 9L, null, false);

        ImportResult result = statementImportService.confirmImport(1L, 5L, List.of(skipped, valid));

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.startDate()).isEqualTo("2025-02-05");
        assertThat(result.endDate()).isEqualTo("2025-02-05");
        verify(transactionService, times(1)).create(eq(1L), any(TransactionRequest.class), eq(true));
    }

    // ── previewStatement happy path (real PDF) ───────────────────────────────

    private MultipartFile pdfWithLines(String... lines) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.setLeading(14f);
                cs.newLineAtOffset(50, 750);
                for (String line : lines) {
                    cs.showText(line);
                    cs.newLine();
                }
                cs.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return new MockMultipartFile("file", "extrato.pdf", "application/pdf", baos.toByteArray());
        }
    }

    @Test
    void previewStatement_pdfComTransacoes_parseiaDebitoECredito() throws IOException {
        when(categoryService.findByAlias(anyLong(), anyString())).thenReturn(List.of());

        MultipartFile file = pdfWithLines(
                "10/01/2025 Mercado Bom Preco - R$ 150,50",
                "20/01/2025 Salario Empresa XYZ + R$ 4.000,00"
        );

        List<ParsedTransactionResponse> rows = statementImportService.previewStatement(1L, file);

        assertThat(rows).hasSize(2);

        ParsedTransactionResponse debit = rows.get(0);
        assertThat(debit.date()).isEqualTo("2025-01-10");
        assertThat(debit.description()).isEqualTo("Mercado Bom Preco");
        assertThat(debit.amount()).isEqualTo(150.50);
        assertThat(debit.type()).isEqualTo(TransactionType.DEBIT);
        assertThat(debit.suggestedCategoryId()).isNull();
        assertThat(debit.hasMultipleSuggestions()).isFalse();

        ParsedTransactionResponse credit = rows.get(1);
        assertThat(credit.date()).isEqualTo("2025-01-20");
        assertThat(credit.description()).isEqualTo("Salario Empresa XYZ");
        assertThat(credit.amount()).isEqualTo(4000.00);
        assertThat(credit.type()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void previewStatement_pdfComSugestaoDeCategoria_preencheCategoria() throws IOException {
        Category cat = new Category();
        cat.setId(8L);
        cat.setName("Alimentacao");
        when(categoryService.findByAlias(eq(1L), eq("Mercado Bom Preco")))
                .thenReturn(List.of(cat));

        MultipartFile file = pdfWithLines("10/01/2025 Mercado Bom Preco - R$ 150,50");

        List<ParsedTransactionResponse> rows = statementImportService.previewStatement(1L, file);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).suggestedCategoryId()).isEqualTo(8L);
        assertThat(rows.get(0).suggestedCategoryName()).isEqualTo("Alimentacao");
        assertThat(rows.get(0).hasMultipleSuggestions()).isFalse();
        assertThat(rows.get(0).allSuggestedCategories()).hasSize(1);
    }

    @Test
    void previewStatement_linhasDeCabecalhoSaoIgnoradas() throws IOException {
        when(categoryService.findByAlias(anyLong(), anyString())).thenReturn(List.of());

        MultipartFile file = pdfWithLines(
                "Extrato de Conta Corrente",
                "Data Lancamento Valor",
                "Saldo anterior 1.000,00",
                "05/02/2025 Conta de Luz - R$ 120,00"
        );

        List<ParsedTransactionResponse> rows = statementImportService.previewStatement(1L, file);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).description()).isEqualTo("Conta de Luz");
        assertThat(rows.get(0).amount()).isEqualTo(120.00);
    }

    @Test
    void previewStatement_arquivoOfx_eDetectadoEParseado() {
        when(categoryService.findByAlias(anyLong(), anyString())).thenReturn(List.of());

        String ofx = "OFXHEADER:100\n<OFX><BANKTRANLIST>"
                + "<STMTTRN><TRNTYPE>DEBIT<DTPOSTED>20250110<TRNAMT>-150.50<MEMO>Mercado</STMTTRN>"
                + "</BANKTRANLIST></OFX>";
        MultipartFile file = new MockMultipartFile("file", "extrato.ofx", "application/octet-stream",
                ofx.getBytes());

        List<ParsedTransactionResponse> rows = statementImportService.previewStatement(1L, file);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).date()).isEqualTo("2025-01-10");
        assertThat(rows.get(0).description()).isEqualTo("Mercado");
        assertThat(rows.get(0).amount()).isEqualTo(150.50);
        assertThat(rows.get(0).type()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void previewStatement_arquivoCnab240_eDetectadoEParseado() {
        when(categoryService.findByAlias(anyLong(), anyString())).thenReturn(List.of());

        char[] rec = new char[240];
        java.util.Arrays.fill(rec, ' ');
        rec[7] = '3';
        rec[13] = 'E';
        "15012025".getChars(0, 8, rec, 142);
        String.format("%018d", 15050L).getChars(0, 18, rec, 150);
        rec[168] = 'D';
        "COMPRA".getChars(0, 6, rec, 169);
        MultipartFile file = new MockMultipartFile("file", "extrato.ret", "application/octet-stream",
                new String(rec).getBytes());

        List<ParsedTransactionResponse> rows = statementImportService.previewStatement(1L, file);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).date()).isEqualTo("2025-01-15");
        assertThat(rows.get(0).amount()).isEqualTo(150.50);
        assertThat(rows.get(0).type()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void previewStatement_blocosDuplicados_saoDeduplicados() throws IOException {
        when(categoryService.findByAlias(anyLong(), anyString())).thenReturn(List.of());

        MultipartFile file = pdfWithLines(
                "10/01/2025 Mercado Bom Preco - R$ 150,50",
                "10/01/2025 Mercado Bom Preco - R$ 150,50"
        );

        List<ParsedTransactionResponse> rows = statementImportService.previewStatement(1L, file);

        assertThat(rows).hasSize(1);
    }
}
