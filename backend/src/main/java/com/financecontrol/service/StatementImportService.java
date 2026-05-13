package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.entity.Category;
import com.financecontrol.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StatementImportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern TX_PATTERN = Pattern.compile(
        "^(\\d{2}/\\d{2}/\\d{4})\\s+(.+)\\s+([+-])\\s*R\\$\\s*([\\d.,]+)\\s*$"
    );

    private static final List<String> SKIP_PREFIXES = List.of(
        "Saldo", "Data ", "Lança", "Movimenta", "Cheque", "Este produto",
        "Fim", "Central", "Não está", "Titular", "Cooperativa", "Momento",
        "Período", "Extrato", "SAC", "Ouvidoria", "ola@", "0800"
    );

    private final TransactionService transactionService;
    private final CategoryService    categoryService;

    public ImportResult statementImport(Long userId, Long accountId, MultipartFile file) {
        List<String> blocks = buildBlocks(extractLines(file));

        int imported  = 0;
        LocalDate minDate = null;
        LocalDate maxDate = null;
        Set<String> seen  = new HashSet<>();

        for (String block : blocks) {
            LocalDate date = processBlock(block.trim(), userId, accountId, seen);
            if (date != null) {
                imported++;
                if (minDate == null || date.isBefore(minDate)) minDate = date;
                if (maxDate == null || date.isAfter(maxDate))  maxDate = date;
            }
        }

        String startDate = minDate != null ? minDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        String endDate   = maxDate != null ? maxDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
        return new ImportResult(imported, startDate, endDate);
    }

    private LocalDate processBlock(String block, Long userId, Long accountId, Set<String> seen) {
        Matcher m = TX_PATTERN.matcher(block);
        if (!seen.add(block) || !m.matches()) return null;

        LocalDate date       = LocalDate.parse(m.group(1), DATE_FMT);
        String categoryLabel = m.group(2).trim();
        char sign            = m.group(3).charAt(0);
        double value         = parseAmount(m.group(4));
        TransactionType type = sign == '+' ? TransactionType.CREDIT : TransactionType.DEBIT;

        Category category = categoryService.findOrCreateByInternalName(userId, categoryLabel);
        transactionService.create(userId, new TransactionRequest(
            accountId, category.getId(), null, value, date, type, 0, null, null
        ));
        return date;
    }

    private List<String> extractLines(MultipartFile file) {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            return Arrays.asList(new PDFTextStripper().getText(doc).split("\\r?\\n"));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read PDF statement", e);
        }
    }

    private List<String> buildBlocks(List<String> lines) {
        List<String> blocks   = new ArrayList<>();
        StringBuilder current = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (shouldSkip(trimmed)) {
                if (current != null) { blocks.add(current.toString()); current = null; }
            } else if (!trimmed.isEmpty() && trimmed.matches("^\\d{2}/\\d{2}/\\d{4}.*")) {
                if (current != null) blocks.add(current.toString());
                current = new StringBuilder(trimmed);
            } else if (!trimmed.isEmpty() && current != null) {
                current.append(" ").append(trimmed);
            }
        }
        if (current != null) blocks.add(current.toString());
        return blocks;
    }

    private boolean shouldSkip(String line) {
        return SKIP_PREFIXES.stream().anyMatch(line::startsWith);
    }

    private double parseAmount(String raw) {
        return Double.parseDouble(raw.replace(".", "").replace(",", "."));
    }
}
