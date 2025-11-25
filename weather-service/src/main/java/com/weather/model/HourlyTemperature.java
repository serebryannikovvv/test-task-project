package com.weather.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

public class HourlyTemperature {
    private final LocalDateTime time;
    private final double temperature;

    @JsonCreator
    public HourlyTemperature(
            @JsonProperty("time") LocalDateTime time,
            @JsonProperty("temperature") double temperature) {

        this.time = time;
        this.temperature = temperature;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getTemperature() {
        return temperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HourlyTemperature)) return false;
        HourlyTemperature that = (HourlyTemperature) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, temperature);
    }

    @Override
    public String toString() {
        return "HourlyTemperature{time=" + time + ", temp=" + temperature + "°C}";
    }
}