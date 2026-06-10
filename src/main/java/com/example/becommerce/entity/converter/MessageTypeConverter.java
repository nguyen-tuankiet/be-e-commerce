package com.example.becommerce.entity.converter;

import com.example.becommerce.entity.enums.MessageType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MessageTypeConverter implements AttributeConverter<MessageType, String> {

    @Override
    public String convertToDatabaseColumn(MessageType attribute) {
        return attribute == null ? null : attribute.apiValue();
    }

    @Override
    public MessageType convertToEntityAttribute(String dbData) {
        return MessageType.from(dbData);
    }
}
