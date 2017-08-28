package com.fx.sharingbikes.user.service;


import com.fx.sharingbikes.common.exception.SharingBikesException;

public interface UserService {

    String login(String data, String key) throws SharingBikesException;
}
