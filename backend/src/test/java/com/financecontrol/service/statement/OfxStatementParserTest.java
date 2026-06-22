package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfxStatementParserTest {

    private static final String OFX = """
            OFXHEADER:100
            DATA:OFXSGML
            VERSION:102
            <OFX>
            <BANKMSGSRSV1><STMTTRNRS><STMTRS><BANKTRANLIST>
            <STMTTRN><TRNTYPE>DEBIT<DTPOSTED>20250110120000<TRNAMT>-150.50<FITID>1<MEMO>Mercado Bom Preco</STMTTRN>
            <STMTTRN><TRNTYPE>CREDIT<DTPOSTED>20250120<TRNAMT>4000.00<FITID>2<NAME>Salario Empresa</STMTTRN>
            </BANKTRANLIST></STMTRS></STMTTRNRS></BANKMSGSRSV1></OFX>
            """;

    @Test
    void looksLikeOfx_porConteudoOuExtensao() {
        byte[] bytes = OFX.getBytes(StandardCharsets.ISO_8859_1);
        assertThat(OfxStatementParser.looksLikeOfx("extrato.ofx", new byte[0])).isTrue();
        assertThat(OfxStatementParser.looksLikeOfx("extrato.bin", bytes)).isTrue();
        assertThat(OfxStatementParser.looksLikeOfx("extrato.pdf", "%PDF".getBytes())).isFalse();
    }

    @Test
    void parse_extraiDebitoECreditoComSinalEDescricao() {
        List<RawTransaction> rows = OfxStatementParser.parse(OFX.getBytes(StandardCharsets.ISO_8859_1));

        assertThat(rows).hasSize(2);

        RawTransaction debit = rows.get(0);
        assertThat(debit.date()).isEqualTo(LocalDate.of(2025, 1, 10));
        assertThat(debit.description()).isEqualTo("Mercado Bom Preco");
        assertThat(debit.amount()).isEqualTo(150.50);
        assertThat(debit.type()).isEqualTo(TransactionType.DEBIT);

        RawTransaction credit = rows.get(1);
        assertThat(credit.date()).isEqualTo(LocalDate.of(2025, 1, 20));
        assertThat(credit.description()).isEqualTo("Salario Empresa");
        assertThat(credit.amount()).isEqualTo(4000.00);
        assertThat(credit.type()).isEqualTo(TransactionType.CREDIT);
    }
}
