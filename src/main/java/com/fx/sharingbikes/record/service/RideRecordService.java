package com.fx.sharingbikes.record.service;

import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.record.entity.RideRecord;

import java.util.List;

public interface RideRecordService {
    List<RideRecord> listRideRecord(Long userId, Long lastId) throws SharingBikesException;
}
