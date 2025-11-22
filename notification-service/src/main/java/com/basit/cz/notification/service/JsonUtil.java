package com.basit.cz.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JsonUtil {

    private static ObjectMapper mapper;

    @Inject
    public JsonUtil(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }

    public static String toJson(Object o) {
        if (o == null) return null;
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
