package com.canvais.api.dto.request;

/**
 * Geographic bounding box enclosing a viewport or drawing area.
 */
public class BoundingBoxDto {

    private double north;
    private double south;
    private double east;
    private double west;

    public BoundingBoxDto() {
    }

    public BoundingBoxDto(double north, double south, double east, double west) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }
}
