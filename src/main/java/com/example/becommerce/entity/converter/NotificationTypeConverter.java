package com.example.becommerce.entity.converter;

import com.example.becommerce.entity.enums.NotificationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationType attribute) {
        return attribute == null ? null : attribute.apiValue();
    }

    @Override
    public NotificationType convertToEntityAttribute(String dbData) {
        return NotificationType.from(dbData);
    }
}
