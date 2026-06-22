package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Cnab240StatementParser {

    private static final DateTimeFormatter CNAB_DATE = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final int RECORD_LENGTH = 240;

    private static final int RECORD_TYPE_POS = 8;
    private static final int SEGMENT_POS     = 14;
    private static final int DATE_START      = 143;
    private static final int DATE_END        = 150;
    private static final int AMOUNT_START    = 151;
    private static final int AMOUNT_END      = 168;
    private static final int DC_POS          = 169;
    private static final int DESC_START      = 170;
    private static final int DESC_END        = 194;

    private Cnab240StatementParser() {}

    public static boolean looksLikeCnab240(String filename, byte[] content) {
        if (filename != null) {
            String f = filename.toLowerCase();
            if (f.endsWith(".cnab") || f.endsWith(".ret") || f.endsWith(".rem")) return true;
        }
        String text = new String(content, StandardCharsets.ISO_8859_1);
        int nl = text.indexOf('\n');
        String firstLine = (nl < 0 ? text : text.substring(0, nl)).replace("\r", "");
        return firstLine.length() == RECORD_LENGTH && firstLine.substring(0, 3).matches("\\d{3}");
    }

    public static List<RawTransaction> parse(byte[] content) {
        String text = new String(content, StandardCharsets.ISO_8859_1);
        List<RawTransaction> result = new ArrayList<>();

        for (String line : text.split("\\r?\\n")) {
            if (line.length() < RECORD_LENGTH) continue;
            if (field(line, RECORD_TYPE_POS, RECORD_TYPE_POS).charAt(0) != '3') continue;
            if (field(line, SEGMENT_POS, SEGMENT_POS).charAt(0) != 'E') continue;

            String rawDate = field(line, DATE_START, DATE_END);
            String rawAmt  = field(line, AMOUNT_START, AMOUNT_END);
            String dc      = field(line, DC_POS, DC_POS);
            String desc    = field(line, DESC_START, DESC_END).trim();

            if (!rawDate.matches("\\d{8}") || rawDate.equals("00000000")) continue;

            LocalDate date = LocalDate.parse(rawDate, CNAB_DATE);
            double amount  = Long.parseLong(rawAmt.trim().isEmpty() ? "0" : rawAmt) / 100.0;
            TransactionType type = "D".equalsIgnoreCase(dc) ? TransactionType.DEBIT : TransactionType.CREDIT;

            result.add(new RawTransaction(date, desc, amount, type));
        }
        return result;
    }

    private static String field(String line, int from1, int to1) {
        int from = from1 - 1;
        int to   = Math.min(to1, line.length());
        if (from >= line.length()) return "";
        return line.substring(from, to);
    }
}
