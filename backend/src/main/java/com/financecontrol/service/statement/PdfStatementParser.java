package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PdfStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern TX_PATTERN = Pattern.compile(
        "^(\\d{2}/\\d{2}/\\d{4})\\s+(.+)\\s+([+-])\\s*R\\$\\s*([\\d.,]+)\\s*$"
    );

    private static final List<String> SKIP_PREFIXES = List.of(
        "Saldo", "Data ", "Lança", "Movimenta", "Cheque", "Este produto",
        "Fim", "Central", "Não está", "Titular", "Cooperativa", "Momento",
        "Período", "Extrato", "SAC", "Ouvidoria", "ola@", "0800"
    );

    private PdfStatementParser() {}

    public static List<RawTransaction> parse(byte[] content) {
        List<RawTransaction> result = new ArrayList<>();
        for (String block : buildBlocks(extractLines(content))) {
            Matcher m = TX_PATTERN.matcher(block.trim());
            if (!m.matches()) continue;

            LocalDate date       = LocalDate.parse(m.group(1), DATE_FMT);
            String description   = m.group(2).trim();
            char sign            = m.group(3).charAt(0);
            double value         = parseAmount(m.group(4));
            TransactionType type = sign == '+' ? TransactionType.CREDIT : TransactionType.DEBIT;

            result.add(new RawTransaction(date, description, value, type));
        }
        return result;
    }

    private static List<String> extractLines(byte[] content) {
        try (PDDocument doc = Loader.loadPDF(content)) {
            return Arrays.asList(new PDFTextStripper().getText(doc).split("\\r?\\n"));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read PDF statement", e);
        }
    }

    private static List<String> buildBlocks(List<String> lines) {
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

    private static boolean shouldSkip(String line) {
        return SKIP_PREFIXES.stream().anyMatch(line::startsWith);
    }

    private static double parseAmount(String raw) {
        return Double.parseDouble(raw.replace(".", "").replace(",", "."));
    }
}
