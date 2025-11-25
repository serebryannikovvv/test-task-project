package com.weather.service;

import com.weather.config.Config;
import com.weather.model.HourlyTemperature;
import com.weather.model.WeatherResponse;

import java.util.List;

public class WeatherService {
    private final GeocodingClient geoClient = new GeocodingClient();
    private final WeatherApiClient weatherClient = new WeatherApiClient();
    private final WeatherCache cache;

    public WeatherService() {
        this.cache = new WeatherCache(Config.redisHost(), Config.redisPort());
    }

    public WeatherResponse getWeather(String city) throws Exception {
        String key = city.trim().toLowerCase();
        WeatherResponse cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        com.weather.model.CityCoordinates coords = geoClient.getCoordinates(city);
        List<HourlyTemperature> hourly = weatherClient.getHourlyTemperature(coords.getLatitude(), coords.getLongitude());

        WeatherResponse response = new WeatherResponse(city, hourly);
        cache.put(key, response);
        return response;
    }
}