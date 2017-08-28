package com.fx.sharingbikes.user.service;


import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.user.entity.User;

public interface UserService {

    String login(String data, String key) throws SharingBikesException;

    void modifyNickName(User user) throws SharingBikesException;
}
