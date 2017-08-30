package com.fx.sharingbikes.user.service;


import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    String login(String data, String key) throws SharingBikesException;

    void modifyNickName(User user) throws SharingBikesException;

    void sendVercode(String mobile, String ip) throws SharingBikesException;

    String uploadHeadImg(MultipartFile file, Long userId) throws SharingBikesException;
}
