package com.fx.sharingbikes.bike.controller;

import com.fx.sharingbikes.bike.entity.Bike;
import com.fx.sharingbikes.bike.entity.BikeLocation;
import com.fx.sharingbikes.bike.entity.Point;
import com.fx.sharingbikes.bike.service.BikeGeoService;
import com.fx.sharingbikes.bike.service.BikeService;
import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.common.resp.ApiResult;
import com.fx.sharingbikes.common.rest.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("bike")
@Slf4j
public class BikeController extends BaseController {

    @Autowired
    @Qualifier("bikeServiceImpl")
    private BikeService bikeService;
//
//    @RequestMapping("generateBike")
//    public void generateBike() {
//        for (int i = 0; i < 50; i++) {
//            try {
//                bikeService.generateBike();
//            } catch (SharingBikesException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Autowired
    private BikeGeoService bikeGeoService;

    @RequestMapping("findAroundBike")
    public ApiResult findAroundBike(@RequestBody Point point) {
        ApiResult<List<BikeLocation>> resp = new ApiResult<>();
        try {
            List<BikeLocation> bikeList = bikeGeoService.geoNear("bike-position", null, point, 10, 100);
            resp.setMessage("查询附近单车成功");
            resp.setData(bikeList);
        } catch (SharingBikesException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to find around bike info", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

    @RequestMapping("unLockBike")
    public ApiResult unLockBike(@RequestBody Bike bike) {
        ApiResult<List<BikeLocation>> resp = new ApiResult<>();
        try {
            bikeService.unLockBike(getCurrentUser(), bike.getNumber());
            resp.setMessage("等待单车解锁");
        } catch (SharingBikesException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to unlock bike", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
        }
        return resp;
    }
}
