package com.weather.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class CityCoordinates {
    private final double latitude;
    private final double longitude;

    @JsonCreator
    public CityCoordinates(
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CityCoordinates)) return false;
        CityCoordinates that = (CityCoordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "CityCoordinates{latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
