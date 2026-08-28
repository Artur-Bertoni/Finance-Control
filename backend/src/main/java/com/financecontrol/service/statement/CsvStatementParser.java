package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class CsvStatementParser {

    private static final char[] DELIMITERS = { ';', ',', '\t', '|' };
    private static final int HEADER_SEARCH_LINES = 20;
    private static final int BINARY_SCAN_BYTES   = 1024;

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("dd/MM/yy"),
        DateTimeFormatter.ofPattern("dd-MM-yy")
    );

    private static final List<String> DATE_KEYS = List.of(
        "data", "date", "dtlancamento", "dtmovimento");
    private static final List<String> TYPE_KEYS = List.of(
        "tipo", "type", "natureza", "debitocredito", "creditodebito", "dc", "cd");
    private static final List<String> BALANCE_KEYS = List.of(
        "saldo", "balance");
    private static final List<String> DEBIT_KEYS = List.of(
        "debito", "debit", "saida", "saque", "retirada");
    private static final List<String> CREDIT_KEYS = List.of(
        "credito", "credit", "entrada", "deposito");
    private static final List<String> AMOUNT_KEYS = List.of(
        "valor", "value", "amount", "montante", "quantia");
    private static final List<String> DESCRIPTION_KEYS = List.of(
        "descricao", "description", "historico", "memo", "lancamento", "detalhe",
        "estabelecimento", "narrativa", "titulo", "observacao");

    private record Layout(char delimiter, int headerLine, int date, int description,
                          int amount, int type, int debit, int credit) {}

    private CsvStatementParser() {}

    public static boolean looksLikeCsv(String filename, byte[] content) {
        if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".csv")) return true;
        if (content.length == 0 || isBinary(content)) return false;
        return findLayout(lines(decode(content))) != null;
    }

    public static List<RawTransaction> parse(byte[] content) {
        List<String> lines = lines(decode(content));
        Layout layout = findLayout(lines);
        if (layout == null) throw new BusinessException("error.statement.csvColumns");

        List<RawTransaction> result = new ArrayList<>();
        for (int i = layout.headerLine() + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            RawTransaction tx = toTransaction(splitLine(line, layout.delimiter()), layout);
            if (tx != null) result.add(tx);
        }
        return result;
    }

    private static RawTransaction toTransaction(List<String> fields, Layout layout) {
        LocalDate date = parseDate(value(fields, layout.date()));
        if (date == null) return null;

        Double signed = null;
        TransactionType columnType = null;

        if (layout.amount() >= 0) signed = parseAmount(value(fields, layout.amount()));
        if ((signed == null || signed == 0) && layout.debit() >= 0) {
            Double debit = parseAmount(value(fields, layout.debit()));
            if (debit != null && debit != 0) {
                signed = -Math.abs(debit);
                columnType = TransactionType.DEBIT;
            }
        }
        if ((signed == null || signed == 0) && layout.credit() >= 0) {
            Double credit = parseAmount(value(fields, layout.credit()));
            if (credit != null && credit != 0) {
                signed = Math.abs(credit);
                columnType = TransactionType.CREDIT;
            }
        }
        if (signed == null || signed == 0) return null;

        TransactionType type = layout.type() >= 0 ? parseType(value(fields, layout.type())) : null;
        if (type == null && layout.amount() >= 0) type = parseSuffixType(value(fields, layout.amount()));
        if (type == null) type = columnType;
        if (type == null) type = signed < 0 ? TransactionType.DEBIT : TransactionType.CREDIT;

        String description = value(fields, layout.description()).replaceAll("\\s+", " ").trim();
        return new RawTransaction(date, description, Math.abs(signed), type);
    }

    private static Layout findLayout(List<String> lines) {
        int limit = Math.min(lines.size(), HEADER_SEARCH_LINES);
        for (int i = 0; i < limit; i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            char delimiter = detectDelimiter(line);
            List<String> headers = splitLine(line, delimiter);
            if (headers.size() < 2) continue;
            Layout layout = toLayout(headers, delimiter, i);
            if (layout != null) return layout;
        }
        return null;
    }

    private static Layout toLayout(List<String> headers, char delimiter, int headerLine) {
        int date = -1;
        int type = -1;
        int debit = -1;
        int credit = -1;
        int amount = -1;
        int description = -1;
        boolean[] used = new boolean[headers.size()];

        for (int i = 0; i < headers.size(); i++) {
            String header = normalize(headers.get(i));
            if (header.isEmpty()) continue;
            if (date < 0 && matches(header, DATE_KEYS))     { date = i;   used[i] = true; continue; }
            if (type < 0 && matches(header, TYPE_KEYS))     { type = i;   used[i] = true; continue; }
            if (matches(header, BALANCE_KEYS))              {             used[i] = true; continue; }
            if (debit < 0 && matches(header, DEBIT_KEYS))   { debit = i;  used[i] = true; continue; }
            if (credit < 0 && matches(header, CREDIT_KEYS)) { credit = i; used[i] = true; continue; }
            if (amount < 0 && matches(header, AMOUNT_KEYS)) { amount = i; used[i] = true; continue; }
            if (description < 0 && matches(header, DESCRIPTION_KEYS)) { description = i; used[i] = true; }
        }

        if (date < 0 || (amount < 0 && debit < 0 && credit < 0)) return null;
        if (description < 0) description = firstUnused(used);
        return new Layout(delimiter, headerLine, date, description, amount, type, debit, credit);
    }

    private static int firstUnused(boolean[] used) {
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) return i;
        }
        return -1;
    }

    private static boolean matches(String header, List<String> keys) {
        for (String key : keys) {
            if (header.equals(key)) return true;
            if (key.length() > 2 && header.contains(key)) return true;
        }
        return false;
    }

    private static char detectDelimiter(String line) {
        char best = ';';
        int bestCount = 0;
        for (char candidate : DELIMITERS) {
            int count = countOutsideQuotes(line, candidate);
            if (count > bestCount) {
                bestCount = count;
                best = candidate;
            }
        }
        return best;
    }

    private static int countOutsideQuotes(String line, char delimiter) {
        int count = 0;
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') quoted = !quoted;
            else if (c == delimiter && !quoted) count++;
        }
        return count;
    }

    private static List<String> splitLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c != '"') current.append(c);
                else if (i + 1 < line.length() && line.charAt(i + 1) == '"') { current.append('"'); i++; }
                else quoted = false;
            } else if (c == '"') {
                quoted = true;
            } else if (c == delimiter) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }

    private static String value(List<String> fields, int index) {
        if (index < 0 || index >= fields.size()) return "";
        return fields.get(index).trim();
    }

    private static LocalDate parseDate(String raw) {
        if (raw.isEmpty()) return null;
        String candidate = raw.split("[ T]")[0].trim();
        if (candidate.isEmpty()) return null;
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(candidate, format);
            } catch (DateTimeParseException e) {
                continue;
            }
        }
        return null;
    }

    private static Double parseAmount(String raw) {
        if (raw.isEmpty()) return null;
        boolean negative = raw.indexOf('-') >= 0 || (raw.startsWith("(") && raw.endsWith(")"));
        String digits = raw.replaceAll("[^0-9.,]", "");
        if (digits.isEmpty()) return null;

        int separator = Math.max(digits.lastIndexOf(','), digits.lastIndexOf('.'));
        String plain;
        if (separator < 0 || isThousandsSeparator(digits, separator)) {
            plain = digits.replaceAll("[^0-9]", "");
        } else {
            plain = digits.substring(0, separator).replaceAll("[^0-9]", "")
                  + "." + digits.substring(separator + 1).replaceAll("[^0-9]", "");
        }
        if (plain.isEmpty() || plain.equals(".")) return null;
        if (plain.startsWith(".")) plain = "0" + plain;

        try {
            double value = Double.parseDouble(plain);
            return negative ? -value : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isThousandsSeparator(String digits, int separator) {
        if (digits.length() - separator - 1 != 3) return false;
        return digits.chars().filter(c -> c == '.' || c == ',').count() == 1;
    }

    private static TransactionType parseType(String raw) {
        String value = normalize(raw);
        if (value.isEmpty()) return null;
        if (value.startsWith("deb") || value.equals("d") || value.startsWith("saida")
            || value.startsWith("saque") || value.startsWith("retirada")) return TransactionType.DEBIT;
        if (value.startsWith("cred") || value.equals("c") || value.startsWith("entrada")
            || value.startsWith("deposito") || value.startsWith("recebimento")) return TransactionType.CREDIT;
        return null;
    }

    private static TransactionType parseSuffixType(String raw) {
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.length() < 2) return null;
        char previous = value.charAt(value.length() - 2);
        if (!Character.isDigit(previous) && !Character.isWhitespace(previous)) return null;
        char last = value.charAt(value.length() - 1);
        if (last == 'D') return TransactionType.DEBIT;
        if (last == 'C') return TransactionType.CREDIT;
        return null;
    }

    private static String normalize(String raw) {
        String stripped = Normalizer.normalize(raw, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return stripped.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static List<String> lines(String text) {
        return Arrays.asList(text.split("\\r?\\n"));
    }

    private static boolean isBinary(byte[] content) {
        int limit = Math.min(content.length, BINARY_SCAN_BYTES);
        for (int i = 0; i < limit; i++) {
            if (content[i] == 0) return true;
        }
        return limit >= 4 && content[0] == '%' && content[1] == 'P' && content[2] == 'D' && content[3] == 'F';
    }

    private static String decode(byte[] content) {
        int offset = hasUtf8Bom(content) ? 3 : 0;
        ByteBuffer buffer = ByteBuffer.wrap(content, offset, content.length - offset);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(buffer)
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(content, offset, content.length - offset, StandardCharsets.ISO_8859_1);
        }
    }

    private static boolean hasUtf8Bom(byte[] content) {
        return content.length >= 3
            && (content[0] & 0xFF) == 0xEF
            && (content[1] & 0xFF) == 0xBB
            && (content[2] & 0xFF) == 0xBF;
    }
}
