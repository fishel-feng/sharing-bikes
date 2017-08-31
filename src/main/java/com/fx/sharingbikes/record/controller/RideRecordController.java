package com.fx.sharingbikes.record.controller;

import com.fx.sharingbikes.bike.service.BikeGeoService;
import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.common.resp.ApiResult;
import com.fx.sharingbikes.common.rest.BaseController;
import com.fx.sharingbikes.record.entity.RideRecord;
import com.fx.sharingbikes.record.service.RideRecordService;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("rideRecord")
@Slf4j
public class RideRecordController extends BaseController {

    @Autowired
    @Qualifier("rideRecordServiceImpl")
    private RideRecordService rideRecordService;

    @Autowired
    private BikeGeoService bikeGeoService;

    @RequestMapping("list/{id}")
    public ApiResult<List<RideRecord>> listRideRecord(@PathVariable("id") Long lastId) {
        ApiResult<List<RideRecord>> resp = new ApiResult<>();
        try {
            UserElement userElement = getCurrentUser();
            List<RideRecord> list = rideRecordService.listRideRecord(userElement.getUserId(), lastId);
            resp.setData(list);
            resp.setMessage("查询成功");
        } catch (SharingBikesException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to query ride record", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

}
