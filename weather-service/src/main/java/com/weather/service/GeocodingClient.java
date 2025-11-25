package com.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.config.Config;
import com.weather.config.JsonMapper;
import com.weather.model.CityCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeocodingClient {
    private static final Logger log = LoggerFactory.getLogger(GeocodingClient.class);

    public CityCoordinates getCoordinates(String city) throws Exception {
        String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8.toString());
        String url = Config.geocodingBaseUrl() + "?name=" + encodedCity + "&count=1&language=en&format=json";

        // ← НОВОЕ: ЛОГИРУЕМ ТОЧНЫЙ URL!
        log.info("Формируем URL для геокодинга: {}", url);

        String jsonResponse;
        try {
            jsonResponse = HttpClientHelper.get(url);
            log.debug("Получен ответ от API (длина: {} символов): {}", jsonResponse.length(), jsonResponse.substring(0, Math.min(200, jsonResponse.length())));
        } catch (Exception e) {
            log.error("Ошибка HTTP-запроса к геокодингу для '{}': {}", city, e.getMessage());
            throw e;
        }

        JsonNode root = JsonMapper.get().readTree(jsonResponse);
        JsonNode results = root.path("results");

        log.debug("Результаты геокодинга для '{}': размер массива = {}", city, results.size());

        if (results.isMissingNode() || results.size() == 0) {
            log.warn("Город '{}' не найден в API — results пустой", city);
            throw new IllegalArgumentException("Город не найден: " + city);
        }

        JsonNode first = results.get(0);
        double lat = first.path("latitude").asDouble();
        double lon = first.path("longitude").asDouble();
        log.info("Найдены координаты для '{}': lat={}, lon={}", city, lat, lon);

        return new CityCoordinates(lat, lon);
    }
}