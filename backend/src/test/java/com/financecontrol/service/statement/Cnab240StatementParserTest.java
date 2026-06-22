package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Cnab240StatementParserTest {

    /** Constrói um registro de detalhe Segmento E com 240 chars usando os offsets do parser. */
    private static String segmentE(String dateDDMMYYYY, long cents, char dc, String desc) {
        char[] rec = new char[240];
        java.util.Arrays.fill(rec, ' ');
        put(rec, 1, "756");                                 // código do banco
        put(rec, 8, "3");                                   // tipo registro
        put(rec, 14, "E");                                  // segmento
        put(rec, 143, dateDDMMYYYY);                        // pos 143-150
        put(rec, 151, String.format("%018d", cents));      // pos 151-168
        put(rec, 169, String.valueOf(dc));                 // pos 169
        put(rec, 170, desc);                               // pos 170-194
        return new String(rec);
    }

    private static void put(char[] rec, int pos1, String value) {
        for (int i = 0; i < value.length() && pos1 - 1 + i < rec.length; i++) {
            rec[pos1 - 1 + i] = value.charAt(i);
        }
    }

    @Test
    void looksLikeCnab240_porLinhaDe240OuExtensao() {
        String rec = segmentE("15012025", 15050, 'D', "COMPRA MERCADO");
        assertThat(Cnab240StatementParser.looksLikeCnab240("arq.bin", rec.getBytes(StandardCharsets.ISO_8859_1))).isTrue();
        assertThat(Cnab240StatementParser.looksLikeCnab240("arq.ret", new byte[0])).isTrue();
        assertThat(Cnab240StatementParser.looksLikeCnab240("arq.pdf", "abc".getBytes())).isFalse();
    }

    @Test
    void parse_leSegmentoEComDebitoECredito() {
        String file = segmentE("15012025", 15050, 'D', "COMPRA MERCADO")
                + "\n"
                + segmentE("20012025", 400000, 'C', "SALARIO");

        List<RawTransaction> rows = Cnab240StatementParser.parse(file.getBytes(StandardCharsets.ISO_8859_1));

        assertThat(rows).hasSize(2);

        RawTransaction debit = rows.get(0);
        assertThat(debit.date()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(debit.amount()).isEqualTo(150.50);
        assertThat(debit.type()).isEqualTo(TransactionType.DEBIT);
        assertThat(debit.description()).isEqualTo("COMPRA MERCADO");

        RawTransaction credit = rows.get(1);
        assertThat(credit.date()).isEqualTo(LocalDate.of(2025, 1, 20));
        assertThat(credit.amount()).isEqualTo(4000.00);
        assertThat(credit.type()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void parse_ignoraRegistrosQueNaoSaoSegmentoE() {
        char[] header = new char[240];
        java.util.Arrays.fill(header, ' ');
        header[7] = '0'; // tipo registro = header de arquivo
        String file = new String(header) + "\n" + segmentE("15012025", 15050, 'D', "COMPRA");

        List<RawTransaction> rows = Cnab240StatementParser.parse(file.getBytes(StandardCharsets.ISO_8859_1));

        assertThat(rows).hasSize(1);
    }
}
