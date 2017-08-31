package com.fx.sharingbikes.record.entity;

import com.fx.sharingbikes.bike.entity.Point;
import lombok.Data;

import java.util.List;

@Data
public class RideContrail {

    private String rideRecordNo;

    private Long bikeNo;

    private List<Point> contrail;
}
