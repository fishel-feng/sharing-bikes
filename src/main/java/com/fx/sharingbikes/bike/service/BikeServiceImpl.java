package com.fx.sharingbikes.bike.service;

import com.fx.sharingbikes.bike.dao.BikeMapper;
import com.fx.sharingbikes.bike.entity.Bike;
import com.fx.sharingbikes.bike.entity.BikeNoGen;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BikeServiceImpl implements BikeService {

    @Autowired
    private BikeMapper bikeMapper;

    @Transactional
    @Override
    public void generateBike() throws SharingBikesException {
        BikeNoGen bikeNoGen = new BikeNoGen();
        bikeMapper.generateBikeNo(bikeNoGen);
        Bike bike = new Bike();
        bike.setType((byte) 1);
        bike.setNumber(bikeNoGen.getAutoIncNo());
        bikeMapper.insertSelective(bike);
    }

}
