package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CreditCardInvoiceParser {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern LINE_START = Pattern.compile("^\\d{2}/[a-z]{3}\\s+\\d{2}:\\d{2}\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_HEAD  = Pattern.compile("^(\\d{2})/([a-z]{3})", Pattern.CASE_INSENSITIVE);
    private static final Pattern AMOUNT     = Pattern.compile("(-?)\\s*R\\$\\s*([\\d.]*\\d,\\d{2})");
    private static final Pattern INSTALLMENT = Pattern.compile("(?<!\\d)(\\d{2}/\\d{2})(?!\\d)");
    private static final Pattern DESC_CUT   = Pattern.compile("\\s+(-?\\s*R\\$|US\\$|BRL)\\b");
    private static final Pattern ANY_FULL_DATE = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");
    private static final Pattern NEXT_CLOSING = Pattern.compile("Fechamento da pr\\p{L}xima fatura\\s+(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern DUE_DATE     = Pattern.compile("Vencimento\\s+(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter REF_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
        Map.entry("jan", 1), Map.entry("fev", 2), Map.entry("mar", 3), Map.entry("abr", 4),
        Map.entry("mai", 5), Map.entry("jun", 6), Map.entry("jul", 7), Map.entry("ago", 8),
        Map.entry("set", 9), Map.entry("out", 10), Map.entry("nov", 11), Map.entry("dez", 12)
    );

    private CreditCardInvoiceParser() {}

    public static boolean looksLikeCreditCardInvoice(String filename, byte[] content) {
        String text = extractText(content);
        String lower = text.toLowerCase(Locale.ROOT);
        return lower.contains("total fatura de") && lower.contains("transa");
    }

    public static List<RawTransaction> parse(byte[] content) {
        return parseText(extractText(content));
    }

    static List<RawTransaction> parseText(String text) {
        List<String> lines = Arrays.asList(text.split("\\r?\\n"));
        LocalDate anchor = findAnchorDate(lines);
        String reference = findInvoiceReference(text);

        List<RawTransaction> result = new ArrayList<>();
        StringBuilder current = null;
        int extraLines = 0;

        for (String raw : lines) {
            String line = raw.trim();
            if (LINE_START.matcher(line).matches()) {
                current = new StringBuilder(line);
                extraLines = 0;
                if (hasAmount(current)) { emit(current.toString(), anchor, reference, result); current = null; }
            } else if (current != null) {
                current.append(' ').append(line);
                if (hasAmount(current)) {
                    emit(current.toString(), anchor, reference, result);
                    current = null;
                } else if (++extraLines > 2) {
                    current = null;
                }
            }
        }
        return result;
    }

    private static void emit(String block, LocalDate anchor, String reference, List<RawTransaction> result) {
        if (block.toLowerCase(Locale.ROOT).contains("pagamento de fatura")) return;

        Matcher dm = DATE_HEAD.matcher(block);
        if (!dm.find()) return;
        Integer month = MONTHS.get(dm.group(2).toLowerCase(Locale.ROOT));
        if (month == null) return;
        int day = Integer.parseInt(dm.group(1));

        LocalDate date = resolveYear(day, month, anchor);
        if (date == null) return;

        String amountRaw = null;
        boolean negative = false;
        Matcher am = AMOUNT.matcher(block);
        while (am.find()) {
            amountRaw = am.group(2);
            negative  = "-".equals(am.group(1));
        }
        if (amountRaw == null) return;

        double value = parseAmount(amountRaw);
        TransactionType type = negative ? TransactionType.CREDIT : TransactionType.DEBIT;

        String installment = null;
        Matcher im = INSTALLMENT.matcher(block);
        if (im.find()) installment = im.group(1);

        result.add(new RawTransaction(date, description(block, installment), value, type, installment, reference));
    }

    private static String findInvoiceReference(String text) {
        Matcher next = NEXT_CLOSING.matcher(text);
        if (next.find()) return refFrom(next.group(1));
        Matcher due = DUE_DATE.matcher(text);
        if (due.find()) return refFrom(due.group(1));
        return null;
    }

    private static String refFrom(String fullDate) {
        LocalDate parsed = tryParse(fullDate);
        return parsed != null ? parsed.minusMonths(1).format(REF_FMT) : null;
    }

    private static String description(String block, String installment) {
        String desc = block.replaceFirst("^\\d{2}/[a-z]{3}\\s+\\d{2}:\\d{2}\\s*", "");
        Matcher cut = DESC_CUT.matcher(desc);
        if (cut.find()) desc = desc.substring(0, cut.start());
        if (installment != null) desc = desc.replace(installment, " ");
        desc = desc.replaceAll("\\s+", " ").trim();
        return desc.isBlank() ? "Lancamento" : desc;
    }

    private static LocalDate resolveYear(int day, int month, LocalDate anchor) {
        int lastDay = LocalDate.of(anchor.getYear(), month, 1).lengthOfMonth();
        int safeDay = Math.min(day, lastDay);
        LocalDate candidate = LocalDate.of(anchor.getYear(), month, safeDay);
        if (candidate.isAfter(anchor)) candidate = candidate.minusYears(1);
        return candidate;
    }

    private static LocalDate findAnchorDate(List<String> lines) {
        for (String line : lines) {
            Matcher m = ANY_FULL_DATE.matcher(line);
            while (m.find()) {
                LocalDate parsed = tryParse(m.group(1));
                if (parsed != null) return parsed;
            }
        }
        return LocalDate.now(ZONE);
    }

    private static LocalDate tryParse(String fullDate) {
        try {
            return LocalDate.parse(fullDate, FULL_DATE);
        } catch (RuntimeException _) {
            return null;
        }
    }

    private static boolean hasAmount(CharSequence block) {
        return AMOUNT.matcher(block).find();
    }

    private static String extractText(byte[] content) {
        try (PDDocument doc = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read credit card invoice PDF", e);
        }
    }

    private static double parseAmount(String raw) {
        return Double.parseDouble(raw.replace(".", "").replace(",", "."));
    }
}
