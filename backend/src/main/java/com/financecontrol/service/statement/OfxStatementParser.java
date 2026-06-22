package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OfxStatementParser {

    private static final DateTimeFormatter OFX_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Pattern STMTTRN =
        Pattern.compile("<STMTTRN>(.*?)</STMTTRN>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private OfxStatementParser() {}

    public static boolean looksLikeOfx(String filename, byte[] content) {
        if (filename != null && filename.toLowerCase().endsWith(".ofx")) return true;
        String head = new String(content, 0, Math.min(content.length, 512), StandardCharsets.ISO_8859_1)
                .toUpperCase();
        return head.contains("OFXHEADER") || head.contains("<OFX>");
    }

    public static List<RawTransaction> parse(byte[] content) {
        String text = new String(content, StandardCharsets.ISO_8859_1);
        List<RawTransaction> result = new ArrayList<>();

        Matcher blocks = STMTTRN.matcher(text);
        while (blocks.find()) {
            String block = blocks.group(1);

            String rawDate = tag(block, "DTPOSTED");
            String rawAmt  = tag(block, "TRNAMT");
            if (rawDate == null || rawAmt == null) continue;

            LocalDate date = LocalDate.parse(rawDate.substring(0, 8), OFX_DATE);
            double signed  = parseAmount(rawAmt);

            String description = firstNonBlank(tag(block, "MEMO"), tag(block, "NAME"));
            if (description == null || description.isBlank()) description = tag(block, "TRNTYPE");
            if (description == null) description = "";

            TransactionType type = signed < 0 ? TransactionType.DEBIT : TransactionType.CREDIT;
            result.add(new RawTransaction(date, description.trim(), Math.abs(signed), type));
        }
        return result;
    }

    private static String tag(String block, String name) {
        Matcher m = Pattern.compile("<" + name + ">([^<\\r\\n]*)", Pattern.CASE_INSENSITIVE).matcher(block);
        return m.find() ? m.group(1).trim() : null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return b;
    }

    private static double parseAmount(String raw) {
        String v = raw.replace(" ", "");
        if (v.indexOf('.') < 0 && v.indexOf(',') >= 0) v = v.replace(',', '.');
        else v = v.replace(",", "");
        return Double.parseDouble(v);
    }
}
