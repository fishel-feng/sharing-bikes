package com.fx.sharingbikes.user.controller;

import com.fx.sharingbikes.user.entity.User;
import com.fx.sharingbikes.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    @RequestMapping("login")
    public String login() {
        return null;
    }
}
