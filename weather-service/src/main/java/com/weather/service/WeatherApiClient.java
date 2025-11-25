package com.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.config.Config;
import com.weather.config.JsonMapper;
import com.weather.model.HourlyTemperature;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherApiClient {
    private final ObjectMapper mapper = JsonMapper.get();

    public List<HourlyTemperature> getHourlyTemperature(double lat, double lon) throws Exception {
        String url = String.format(Locale.US,
                Config.forecastBaseUrl() + "?latitude=%f&longitude=%f&hourly=temperature_2m&forecast_days=1",
                lat, lon);
        String json = HttpClientHelper.get(url);

        JsonNode root = mapper.readTree(json);
        JsonNode times = root.path("hourly").path("time");
        JsonNode temps = root.path("hourly").path("temperature_2m");

        List<HourlyTemperature> list = new ArrayList<>();
        int count = Math.min(24, times.size());
        for (int i = 0; i < count; i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i).asText());
            double temp = temps.get(i).asDouble();
            list.add(new HourlyTemperature(time, temp));
        }
        return list;
    }
}