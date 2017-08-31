package com.fx.sharingbikes.bike.service;

import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.user.entity.UserElement;

public interface BikeService {
    void generateBike() throws SharingBikesException;

    void unLockBike(UserElement currentUser, Long bikeNo) throws SharingBikesException;

    void lockBike(Long bikeNo) throws SharingBikesException;
}
