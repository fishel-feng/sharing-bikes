package com.fx.sharingbikes.bike.dao;

import com.fx.sharingbikes.bike.entity.Bike;
import com.fx.sharingbikes.bike.entity.BikeNoGen;

public interface BikeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Bike record);

    int insertSelective(Bike record);

    Bike selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Bike record);

    int updateByPrimaryKey(Bike record);

    void generateBikeNo(BikeNoGen bikeNoGen);

    Bike selectByBikeNo(Long bikeNo);
}