package ru.hzerr.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode read(String content) throws JsonProcessingException {
        return MAPPER.readTree(content);
    }

    public static <T> T read(String content, Class<T> valueType) throws JsonProcessingException {
        return MAPPER.readValue(content, valueType);
    }

    public static <T> T read(String content, TypeReference<T> valueType) throws JsonProcessingException {
        return MAPPER.readValue(content, valueType);
    }

    public static <T> boolean isJson(String content, Class<T> valueType) {
        try {
            MAPPER.readValue(content, valueType);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
