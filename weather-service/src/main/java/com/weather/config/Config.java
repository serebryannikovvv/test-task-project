package com.weather.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = Config.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new RuntimeException("Не найден файл application.properties в classpath");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения application.properties", e);
        }
    }

    private Config() {
    }

    public static int serverPort() {
        return Integer.parseInt(props.getProperty("server.port", "8080"));
    }

    public static String redisHost() {
        return props.getProperty("redis.host", "localhost");
    }

    public static int redisPort() {
        return Integer.parseInt(props.getProperty("redis.port", "6379"));
    }

    public static int redisTtlSeconds() {
        return Integer.parseInt(props.getProperty("redis.cache.ttl.seconds", "900"));
    }

    public static String geocodingBaseUrl() {
        return props.getProperty("api.geocoding.base-url");
    }

    public static String forecastBaseUrl() {
        return props.getProperty("api.forecast.base-url");
    }
}
