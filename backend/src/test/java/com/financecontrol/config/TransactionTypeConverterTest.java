package com.financecontrol.config;

import com.financecontrol.enums.TransactionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TransactionTypeConverterTest {

    private final TransactionTypeConverter converter = new TransactionTypeConverter();

    @Test
    void convertToDatabaseColumn_debit() {
        assertThat(converter.convertToDatabaseColumn(TransactionType.DEBIT))
                .isEqualTo(TransactionType.DEBIT.getCode());
    }

    @Test
    void convertToDatabaseColumn_credit() {
        assertThat(converter.convertToDatabaseColumn(TransactionType.CREDIT))
                .isEqualTo(TransactionType.CREDIT.getCode());
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_debit() {
        assertThat(converter.convertToEntityAttribute(TransactionType.DEBIT.getCode()))
                .isEqualTo(TransactionType.DEBIT);
    }

    @Test
    void convertToEntityAttribute_credit() {
        assertThat(converter.convertToEntityAttribute(TransactionType.CREDIT.getCode()))
                .isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void convertToEntityAttribute_null() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
