package com.financecontrol.config;

import com.financecontrol.enums.TransactionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionTypeConverter implements AttributeConverter<TransactionType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TransactionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public TransactionType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return TransactionType.fromCode(dbData);
    }
}
