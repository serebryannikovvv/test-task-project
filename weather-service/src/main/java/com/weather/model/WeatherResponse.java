package com.weather.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WeatherResponse {
    private final String city;
    private final List<HourlyTemperature> hourly;

    @JsonCreator
    public WeatherResponse(
            @JsonProperty("city")String city,
            @JsonProperty("hourly") List<HourlyTemperature> hourly) {
        this.city = city;
        this.hourly = Collections.unmodifiableList(hourly);
    }

    public String getCity() { return city; }
    public List<HourlyTemperature> getHourly() { return hourly; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherResponse)) return false;
        WeatherResponse that = (WeatherResponse) o;
        return Objects.equals(city, that.city) && Objects.equals(hourly, that.hourly);
    }

    @Override public int hashCode() {
        return Objects.hash(city, hourly);
    }

    @Override public String toString() {
        return "WeatherResponse{city='" + city + "', hourly=" + hourly.size() + " entries}";
    }
}
