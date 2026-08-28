package com.financecontrol.service;

import com.financecontrol.dto.request.ImportRowRequest;
import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.CategorySuggestionDto;
import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.dto.response.ParsedTransactionResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.service.statement.Cnab240StatementParser;
import com.financecontrol.service.statement.CreditCardInvoiceParser;
import com.financecontrol.service.statement.CsvStatementParser;
import com.financecontrol.service.statement.OfxStatementParser;
import com.financecontrol.service.statement.PdfStatementParser;
import com.financecontrol.service.statement.RawTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StatementImportService {

    private static final List<String> SUPPORTED_EXTENSIONS =
        List.of(".pdf", ".ofx", ".csv", ".cnab", ".ret", ".rem", ".txt");

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<ParsedTransactionResponse> previewStatement(Long userId,
                                                            MultipartFile file) {
        List<RawTransaction> raw = parse(file);
        List<ParsedTransactionResponse> rows = new ArrayList<>();

        for (RawTransaction tx : raw) {
            rows.add(toResponse(tx, userId));
        }
        return rows;
    }

    @Transactional
    public ImportResult confirmImport(Long userId,
                                      Long accountId,
                                      List<ImportRowRequest> rows) {
        int imported = 0;
        LocalDate minDate = null;
        LocalDate maxDate = null;

        for (ImportRowRequest row : rows) {
            if (row.skip() || row.categoryId() == null) continue;

            categoryService.learnAlias(userId, row.description(), row.categoryId());

            LocalDate date = LocalDate.parse(row.date());
            String obs = row.installmentLabel() != null && !row.installmentLabel().isBlank()
                    ? "Parcela " + row.installmentLabel() : null;
            transactionService.create(userId, new TransactionRequest(
                accountId, row.categoryId(), row.localeId(), row.amount(), date, row.type(), 0, obs, null,
                row.invoiceReference()
            ), true);
            imported++;
            if (minDate == null || date.isBefore(minDate)) minDate = date;
            if (maxDate == null || date.isAfter(maxDate))  maxDate = date;
        }

        String startDate = minDate != null ? minDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        String endDate   = maxDate != null ? maxDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        return new ImportResult(imported, startDate, endDate);
    }

    private List<RawTransaction> parse(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read statement file", e);
        }
        String filename = file.getOriginalFilename();
        requireSupportedExtension(filename);

        if (OfxStatementParser.looksLikeOfx(filename, bytes))      return OfxStatementParser.parse(bytes);
        if (Cnab240StatementParser.looksLikeCnab240(filename, bytes)) return Cnab240StatementParser.parse(bytes);
        if (CsvStatementParser.looksLikeCsv(filename, bytes)) return CsvStatementParser.parse(bytes);
        if (CreditCardInvoiceParser.looksLikeCreditCardInvoice(filename, bytes)) return CreditCardInvoiceParser.parse(bytes);
        return PdfStatementParser.parse(bytes);
    }

    private void requireSupportedExtension(String filename) {
        String name = filename != null ? filename.toLowerCase(Locale.ROOT) : "";
        boolean supported = SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (!supported) throw new BusinessException("error.statement.unsupportedFile");
    }

    private ParsedTransactionResponse toResponse(RawTransaction tx,
                                                 Long userId) {
        List<Category> suggestions = categoryService.findByAlias(userId, tx.description());
        Category first = suggestions.isEmpty() ? null : suggestions.getFirst();

        List<CategorySuggestionDto> allSuggestions = suggestions.stream()
                .map(c -> new CategorySuggestionDto(c.getId(), c.getName()))
                .toList();

        return new ParsedTransactionResponse(
            tx.date().format(DateTimeFormatter.ISO_LOCAL_DATE),
            tx.description(),
            tx.amount(),
            tx.type(),
            first != null ? first.getId() : null,
            first != null ? first.getName() : null,
            suggestions.size() > 1,
            allSuggestions,
            tx.installmentLabel(),
            tx.invoiceReference()
        );
    }
}
