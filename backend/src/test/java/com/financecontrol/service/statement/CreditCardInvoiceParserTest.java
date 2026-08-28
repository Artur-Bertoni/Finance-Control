package com.financecontrol.service.statement;

import com.financecontrol.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreditCardInvoiceParserTest {

    private static final String FATURA = """
            Total fatura de junho R$ 1.109,26
            Fechamento da próxima fatura 24/06/2026
            Transações
            Cartão Artur B Dos Santos (final 9120)
            Data e hora Cidade Compra Descrição Parcela Valor em reais
            24/mai 22:29 Anuidade Diferenc 01/12\s
            9120 R$ 5,83
            21/mai 12:07 Porto Alegre Presencial Baguetao Grelhados R$ 83,00
            08/mai 05:16 Pagamento De Fatura -R$ 661,68
            Total cartão (final 9120) R$ 574,54
            Cartão virtual Artur B Dos Santos (final 9914)
            24/jun 01:12 Railway Railway Com Ca US$ 18,30 R$ 5,23 R$ 95,64
            09/jun 18:31 Online Anthropic Claude Sub Anthropic Com BRL 110,00 = US$ 0,00 R$ 0,00 R$ 110,00
            24/jun 00:46 Iof Compra Internacional R$ 3,34
            """;

    private RawTransaction find(List<RawTransaction> txs, String descPart) {
        return txs.stream().filter(t -> t.description().contains(descPart)).findFirst().orElseThrow();
    }

    @Test
    void parseText_extraiComprasIgnorandoCabecalhosTotaisEPagamentoDeFatura() {
        List<RawTransaction> txs = CreditCardInvoiceParser.parseText(FATURA);

        assertThat(txs).hasSize(5);
        assertThat(txs).noneMatch(t -> t.description().startsWith("Total"));
    }

    @Test
    void parseText_pagamentoDeFatura_ehIgnorado() {
        assertThat(CreditCardInvoiceParser.parseText(FATURA))
                .noneMatch(t -> t.description().toLowerCase().contains("pagamento de fatura"));
    }

    @Test
    void parseText_anuidade_preservaParcelaEInfereAno() {
        RawTransaction anuidade = find(CreditCardInvoiceParser.parseText(FATURA), "Anuidade");
        assertThat(anuidade.installmentLabel()).isEqualTo("01/12");
        assertThat(anuidade.amount()).isEqualTo(5.83);
        assertThat(anuidade.type()).isEqualTo(TransactionType.DEBIT);
        assertThat(anuidade.date()).isEqualTo(LocalDate.of(2026, 5, 24));
    }

    @Test
    void parseText_exterior_usaColunaEmReais() {
        List<RawTransaction> txs = CreditCardInvoiceParser.parseText(FATURA);
        assertThat(find(txs, "Anthropic").amount()).isEqualTo(110.00);
        assertThat(find(txs, "Railway").amount()).isEqualTo(95.64);
    }

    private static final String FATURA_JULHO_COMPLETA = """
            Total fatura de julho R$ 987,63
            Fechamento da próxima fatura 24/07/2026
            Transações
            Cartão Artur B Dos Santos (final 9120)
            24/jun 00:46 Anuidade Diferenc 02/12\s
            9120 R$ 5,83
            20/jun 20:43 Porto Alegre Presencial Zafari Higienop R$ 39,25
            19/jun 20:47 Sao Paulo Online Amazon Prime Aluguel R$ 11,90
            14/jun 21:50 Porto Alegre Presencial Gpa Bar E Restaurante R$ 280,79
            08/jun 05:15 Pagamento De Fatura -R$ 1.109,26
            22/mai 20:20 Curitiba Presencial G R Comercio De Me 01/02 R$ 100,00
            Total cartão (final 9120) R$ 437,77
            Cartão virtual Artur B Dos Santos (final 9914)
            24/jun 01:12 Railway Railway Com Ca US$ 18,30 R$ 5,23 R$ 95,64
            24/jun 00:46 Iof Compra Internacional R$ 3,34
            10/jun 21:47 Iof Compra Internacional R$ 3,85
            09/jun 18:31 Online Anthropic Claude Sub Anthropic Com BRL 110,00 = US$ 0,00 R$ 0,00 R$ 110,00
            08/jun 19:16 Sao Paulo Online Amazon Br Amazon Br R$ 46,26
            28/mai 09:56 Sao Paulo Online Dl Uberrides R$ 26,98
            26/mai 07:41 Sao Paulo Online Dl Uberrides R$ 20,99
            26/mai 17:03 Sao Paulo Online Dl Uberrides R$ 19,97
            25/mai 22:39 Iof Compra Internacional R$ 3,77
            25/mai 22:39 Iof Compra Internacional R$ 0,88
            24/mai 01:12 Railway Railway Com Ca US$ 5,00 R$ 5,07 R$ 25,36
            23/mai 16:38 Sao Paulo Online Dl Uberrides R$ 37,99
            23/mai 20:14 Sao Paulo Online Dl Uberrides R$ 32,94
            23/mai 16:20 Online Steamgames Com 4259522 912\s
            1844160 BRL 107,99 = US$ 0,00 R$ 0,00 R$ 107,99
            Total cartão virtual (final 9914) R$ 535,96
            Cartão (final 9112)
            10/jul 00:30 Online Amazon Prime Br 12/12 R$ 13,90
            Total cartão (final 9112) R$ 13,90
            """;

    @Test
    void parseText_faturaCompletaDeJulho_somaBateComOTotalDoBanco() {
        List<RawTransaction> txs = CreditCardInvoiceParser.parseText(FATURA_JULHO_COMPLETA);

        assertThat(txs).hasSize(20);
        double total = txs.stream().mapToDouble(RawTransaction::amount).sum();
        assertThat(total).isEqualTo(987.63, org.assertj.core.api.Assertions.offset(0.001));

        assertThat(find(txs, "G R Comercio").installmentLabel()).isEqualTo("01/02");
        assertThat(find(txs, "Steamgames").amount()).isEqualTo(107.99);
        assertThat(find(txs, "Amazon Prime Br").installmentLabel()).isEqualTo("12/12");
    }

    @Test
    void parseText_descricaoNaoCarregaValoresNemMoedas() {
        List<RawTransaction> txs = CreditCardInvoiceParser.parseText(FATURA_JULHO_COMPLETA);

        assertThat(txs).noneMatch(t -> t.description().contains("R$"));
        assertThat(txs).noneMatch(t -> t.description().contains("BRL"));
        assertThat(txs).noneMatch(t -> t.description().matches(".*\\d,\\d\\d.*"));

        assertThat(find(txs, "Zafari").description()).isEqualTo("Porto Alegre Presencial Zafari Higienop");
        assertThat(find(txs, "Gpa Bar").description()).isEqualTo("Porto Alegre Presencial Gpa Bar E Restaurante");
        assertThat(find(txs, "Railway").description()).isEqualTo("Railway Railway Com Ca");
        assertThat(find(txs, "Anthropic").description()).isEqualTo("Online Anthropic Claude Sub Anthropic Com");
        assertThat(find(txs, "G R Comercio").description()).isEqualTo("Curitiba Presencial G R Comercio De Me");
        assertThat(find(txs, "Amazon Prime Br").description()).isEqualTo("Online Amazon Prime Br");
    }

    @Test
    void parseText_carimbaReferenciaDaFatura() {
        List<RawTransaction> txs = CreditCardInvoiceParser.parseText(FATURA_JULHO_COMPLETA);
        assertThat(txs).allMatch(t -> "2026-06".equals(t.invoiceReference()));
        assertThat(find(txs, "Amazon Prime Br").invoiceReference()).isEqualTo("2026-06");
    }
}
