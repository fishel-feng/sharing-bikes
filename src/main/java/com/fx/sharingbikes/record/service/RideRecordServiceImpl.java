package com.fx.sharingbikes.record.service;

import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.record.dao.RideRecordMapper;
import com.fx.sharingbikes.record.entity.RideRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RideRecordServiceImpl implements RideRecordService {

    @Autowired
    private RideRecordMapper rideRecordMapper;

    @Override
    public List<RideRecord> listRideRecord(Long userId, Long lastId) throws SharingBikesException {
        List<RideRecord> list = rideRecordMapper.selectRideRecordPage(userId, lastId);
        return list;
    }
}
