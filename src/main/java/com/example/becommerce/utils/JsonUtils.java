package com.example.becommerce.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * JSON conversion helper for map payloads.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public String toJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            log.warn("Cannot serialize notification data to JSON", ex);
            return "{}";
        }
    }

    public Map<String, Object> toMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            log.warn("Cannot deserialize notification data JSON: {}", json, ex);
            return Collections.emptyMap();
        }
    }
}

