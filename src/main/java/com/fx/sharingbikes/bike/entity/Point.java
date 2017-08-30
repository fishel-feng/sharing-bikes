package com.fx.sharingbikes.bike.entity;

import lombok.Data;

@Data
public class Point {

    public Point() {
    }

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    private double longitude;

    private double latitude;
}
