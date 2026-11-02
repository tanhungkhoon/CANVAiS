package com.canvais.api.dto.request;

/**
 * Single coordinate point within a freehand drawing gesture sequence.
 */
public class GesturePointDto {

    private double lat;
    private double lng;
    private Long timestampMs;

    public GesturePointDto() {
    }

    public GesturePointDto(double lat, double lng, Long timestampMs) {
        this.lat = lat;
        this.lng = lng;
        this.timestampMs = timestampMs;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(Long timestampMs) {
        this.timestampMs = timestampMs;
    }
}
