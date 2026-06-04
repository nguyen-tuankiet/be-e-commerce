package com.example.becommerce.entity.converter;

import com.example.becommerce.entity.enums.QuotationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class QuotationStatusConverter implements AttributeConverter<QuotationStatus, String> {

    @Override
    public String convertToDatabaseColumn(QuotationStatus attribute) {
        return attribute == null ? null : attribute.apiValue();
    }

    @Override
    public QuotationStatus convertToEntityAttribute(String dbData) {
        return QuotationStatus.from(dbData);
    }
}
