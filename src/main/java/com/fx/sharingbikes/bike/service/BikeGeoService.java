package com.fx.sharingbikes.bike.service;

import com.fx.sharingbikes.bike.entity.BikeLocation;
import com.fx.sharingbikes.bike.entity.Point;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.record.entity.RideContrail;
import com.mongodb.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BikeGeoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<BikeLocation> geoNearSphere(String collection, String locationField, Point center, long minDistance, long maxDistance, DBObject query, DBObject fields, int limit) throws SharingBikesException {
        try {
            if (query == null) {
                query = new BasicDBObject();
            }
            query.put(locationField,
                    new BasicDBObject("$nearSphere",
                            new BasicDBObject("$geometry",
                                    new BasicDBObject("type", "Point")
                                            .append("coordinates", new double[]{center.getLongitude(), center.getLatitude()}))
                                    .append("$minDistance", minDistance)
                                    .append("$maxDistance", maxDistance)
                    ));
            query.put("status", 1);
            List<DBObject> objList = mongoTemplate.getCollection(collection).find(query, fields).limit(limit).toArray();
            List<BikeLocation> result = new ArrayList<>();
            for (DBObject obj : objList) {
                BikeLocation location = new BikeLocation();
                location.setBikeNumber(((Integer) obj.get("bike_no")).longValue());
                location.setStatus((Integer) obj.get("status"));
                BasicDBList coordinates = (BasicDBList) ((BasicDBObject) obj.get("location")).get("coordinates");
                Double[] temp = new Double[2];
                coordinates.toArray(temp);
                location.setCoordinates(temp);
                result.add(location);
            }
            return result;
        } catch (Exception e) {
            log.error("Fail to find around bike", e);
            throw new SharingBikesException("查找附近单车失败");
        }
    }

    public List<BikeLocation> geoNear(String collection, DBObject query, Point point, int limit, long maxDistance) throws SharingBikesException {
        try {
            if (query == null) {
                query = new BasicDBObject();
            }
            List<DBObject> pipeLine = new ArrayList<>();
            BasicDBObject aggregate = new BasicDBObject("$geoNear",
                    new BasicDBObject("near", new BasicDBObject("type", "Point").append("coordinates", new double[]{point.getLongitude(), point.getLatitude()}))
                            .append("distanceField", "distance")
                            .append("num", limit)
                            .append("maxDistance", maxDistance)
                            .append("spherical", true)
                            .append("query", new BasicDBObject("status", 1))
            );
            pipeLine.add(aggregate);
            Cursor cursor = mongoTemplate.getCollection(collection).aggregate(pipeLine, AggregationOptions.builder().build());
            List<BikeLocation> result = new ArrayList<>();
            while (cursor.hasNext()) {
                DBObject obj = cursor.next();
                BikeLocation location = new BikeLocation();
                location.setBikeNumber(((Integer) obj.get("bike_no")).longValue());
                BasicDBList coordinates = (BasicDBList) ((BasicDBObject) obj.get("location")).get("coordinates");
                Double[] temp = new Double[2];
                coordinates.toArray(temp);
                location.setCoordinates(temp);
                location.setDistance((Double) obj.get("distance"));
                result.add(location);
            }

            return result;
        } catch (Exception e) {
            log.error("Fail to find around bike", e);
            throw new SharingBikesException("查找附近单车失败");
        }
    }

    public RideContrail rideContrail(String collection, String recordNo) throws SharingBikesException {
        try {
            DBObject object = mongoTemplate.getCollection(collection).findOne(new BasicDBObject("record_no", recordNo));
            RideContrail rideContrail = new RideContrail();
            rideContrail.setRideRecordNo((String) object.get("record_no"));
            rideContrail.setBikeNo(((Integer) object.get("bike_no")).longValue());
            BasicDBList locList = (BasicDBList) object.get("contrail");
            List<Point> pointList = new ArrayList<>();
            for (Object o : locList) {
                BasicDBList locObj = (BasicDBList) ((BasicDBObject) o).get("loc");
                Double[] temp=new Double[2];
                locObj.toArray(temp);
                Point point=new Point(temp);
                pointList.add(point);
            }
            rideContrail.setContrail(pointList);
            return rideContrail;
        }catch (Exception e){
            log.error("Fail to query ride contrail",e);
            throw new SharingBikesException("查询单车轨迹失败");
        }
    }
}
