package com.example.becommerce.entity.converter;

import com.example.becommerce.entity.enums.ConversationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConversationStatusConverter implements AttributeConverter<ConversationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ConversationStatus attribute) {
        return attribute == null ? null : attribute.apiValue();
    }

    @Override
    public ConversationStatus convertToEntityAttribute(String dbData) {
        return ConversationStatus.from(dbData);
    }
}
