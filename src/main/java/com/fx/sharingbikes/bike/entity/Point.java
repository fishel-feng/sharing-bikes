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

    public Point(Double[] loc){
        this.longitude = loc[0];
        this.latitude = loc[1];
    }

    private double longitude;

    private double latitude;
}
