package com.financecontrol.service;

import com.financecontrol.dto.request.ImportRowRequest;
import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.CategorySuggestionDto;
import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.dto.response.ParsedTransactionResponse;
import com.financecontrol.entity.Category;
import com.financecontrol.service.statement.Cnab240StatementParser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatementImportService {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<ParsedTransactionResponse> previewStatement(Long userId,
                                                            MultipartFile file) {
        List<RawTransaction> raw = parse(file);
        List<ParsedTransactionResponse> rows = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (RawTransaction tx : raw) {
            ParsedTransactionResponse row = toResponse(tx, userId, seen);
            if (row != null) rows.add(row);
        }
        return rows;
    }

    @Transactional
    @SuppressWarnings("null")
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
            transactionService.create(userId, new TransactionRequest(
                accountId, row.categoryId(), row.localeId(), row.amount(), date, row.type(), 0, null, null
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

        if (OfxStatementParser.looksLikeOfx(filename, bytes))      return OfxStatementParser.parse(bytes);
        if (Cnab240StatementParser.looksLikeCnab240(filename, bytes)) return Cnab240StatementParser.parse(bytes);
        return PdfStatementParser.parse(bytes);
    }

    private ParsedTransactionResponse toResponse(RawTransaction tx,
                                                 Long userId,
                                                 Set<String> seen) {
        String key = tx.date() + "|" + tx.description() + "|" + tx.amount() + "|" + tx.type();
        if (!seen.add(key)) return null;

        List<Category> suggestions = categoryService.findByAlias(userId, tx.description());
        Category first = suggestions.isEmpty() ? null : suggestions.get(0);

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
            allSuggestions
        );
    }
}
