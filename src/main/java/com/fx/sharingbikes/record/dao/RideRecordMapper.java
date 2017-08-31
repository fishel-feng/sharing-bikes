package com.fx.sharingbikes.record.dao;

import com.fx.sharingbikes.record.entity.RideRecord;

public interface RideRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RideRecord record);

    int insertSelective(RideRecord record);

    RideRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RideRecord record);

    int updateByPrimaryKey(RideRecord record);

    RideRecord selectRecordNotClosed(Long userId);

    RideRecord selectBikeRecordOnGoing(Long bikeNo);
}