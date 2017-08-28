package com.fx.sharingbikes.user.service;

import com.fx.sharingbikes.user.dao.UserMapper;
import com.fx.sharingbikes.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String login() {
        return null;
    }
}
