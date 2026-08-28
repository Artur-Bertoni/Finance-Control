package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvStatementParserTest {

    private static final String SICREDI = """
            Data;Descricao;CodTransacao;Identificador;Tipo;Valor;Saldo
            28/08/2026;CARTAO DEBITO - ARMAZEM FAZOLIN - BR;138;TRA-57681742-20260828-624014677382;DEBITO;- R$ 46,11;R$ 822,21
            26/08/2026;PAGAMENTO DE FATURA CARTAO CREDITO VIA DEBITO - Sicredi Origens RS - 927965;338;e950e581-3f7a-4443-a2f5-5af459173944;DEBITO;- R$ 522,55;R$ 1.007,30
            20/08/2026;SALARIO EMPRESA;220;E4339441920260820151;CREDITO;R$ 3.450,00;R$ 4.457,18
            """;

    private static byte[] withBom(String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
            out.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    @Test
    void looksLikeCsv_porExtensaoOuCabecalho() {
        assertThat(CsvStatementParser.looksLikeCsv("extrato.csv", new byte[0])).isTrue();
        assertThat(CsvStatementParser.looksLikeCsv("extrato.txt", withBom(SICREDI))).isTrue();
        assertThat(CsvStatementParser.looksLikeCsv("extrato.pdf", "%PDF-1.4 conteudo".getBytes(StandardCharsets.ISO_8859_1))).isFalse();
        assertThat(CsvStatementParser.looksLikeCsv("qualquer.txt", "linha sem cabecalho\noutra linha".getBytes(StandardCharsets.UTF_8))).isFalse();
    }

    @Test
    void parse_extratoSicrediComBomEMoedaBrasileira() {
        List<RawTransaction> rows = CsvStatementParser.parse(withBom(SICREDI));

        assertThat(rows).hasSize(3);

        RawTransaction first = rows.get(0);
        assertThat(first.date()).isEqualTo(LocalDate.of(2026, 8, 28));
        assertThat(first.description()).isEqualTo("CARTAO DEBITO - ARMAZEM FAZOLIN - BR");
        assertThat(first.amount()).isEqualTo(46.11);
        assertThat(first.type()).isEqualTo(TransactionType.DEBIT);

        RawTransaction second = rows.get(1);
        assertThat(second.amount()).isEqualTo(522.55);
        assertThat(second.type()).isEqualTo(TransactionType.DEBIT);

        RawTransaction third = rows.get(2);
        assertThat(third.date()).isEqualTo(LocalDate.of(2026, 8, 20));
        assertThat(third.description()).isEqualTo("SALARIO EMPRESA");
        assertThat(third.amount()).isEqualTo(3450.00);
        assertThat(third.type()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void parse_ignoraLinhasSemDataOuSemValor() {
        String csv = """
                Extrato de Conta Corrente
                Periodo: 01/08/2026 a 31/08/2026

                Data;Historico;Valor;Saldo
                Saldo anterior;;;R$ 1.000,00
                05/08/2026;Conta de Luz;- R$ 120,00;R$ 880,00
                ;Linha sem data;- R$ 50,00;
                06/08/2026;Estorno sem valor;;R$ 880,00
                """;

        List<RawTransaction> rows = CsvStatementParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).description()).isEqualTo("Conta de Luz");
        assertThat(rows.get(0).amount()).isEqualTo(120.00);
        assertThat(rows.get(0).type()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void parse_colunasSeparadasDeDebitoECredito() {
        String csv = """
                "Data";"Historico";"Debito";"Credito"
                "05/08/2026";"Mercado; Bom Preco";"150,50";""
                "10/08/2026";"Salario";"";"4.000,00"
                """;

        List<RawTransaction> rows = CsvStatementParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).description()).isEqualTo("Mercado; Bom Preco");
        assertThat(rows.get(0).amount()).isEqualTo(150.50);
        assertThat(rows.get(0).type()).isEqualTo(TransactionType.DEBIT);
        assertThat(rows.get(1).amount()).isEqualTo(4000.00);
        assertThat(rows.get(1).type()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void parse_formatoInternacionalComVirgulaComoSeparadorDeCampo() {
        String csv = """
                Date,Description,Amount
                2026-08-05,Grocery Store,-150.50
                2026-08-10,Payroll,4000.00
                """;

        List<RawTransaction> rows = CsvStatementParser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).date()).isEqualTo(LocalDate.of(2026, 8, 5));
        assertThat(rows.get(0).description()).isEqualTo("Grocery Store");
        assertThat(rows.get(0).amount()).isEqualTo(150.50);
        assertThat(rows.get(0).type()).isEqualTo(TransactionType.DEBIT);
        assertThat(rows.get(1).amount()).isEqualTo(4000.00);
        assertThat(rows.get(1).type()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void parse_arquivoLatin1SemCabecalhoReconhecido_lancaBusinessException() {
        byte[] semColunas = "Coluna A;Coluna B;Coluna C\n1;2;3\n".getBytes(StandardCharsets.ISO_8859_1);

        assertThatThrownBy(() -> CsvStatementParser.parse(semColunas))
                .isInstanceOf(BusinessException.class)
                .hasMessage("error.statement.csvColumns");
    }

    @Test
    void parse_acentuacaoLatin1EhLidaCorretamente() {
        byte[] content = "Data;Descrição;Valor\n05/08/2026;Água e Esgoto;- R$ 90,00\n"
                .getBytes(StandardCharsets.ISO_8859_1);

        List<RawTransaction> rows = CsvStatementParser.parse(content);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).description()).isEqualTo("Água e Esgoto");
        assertThat(rows.get(0).amount()).isEqualTo(90.00);
    }
}
