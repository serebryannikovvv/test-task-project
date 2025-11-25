package com.weather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

public final class JsonMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JSR310Module());
    }

    private JsonMapper() {}

    public static ObjectMapper get() {
        return mapper;
    }
}
