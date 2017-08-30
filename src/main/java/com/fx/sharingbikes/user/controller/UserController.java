package com.fx.sharingbikes.user.controller;

import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.common.resp.ApiResult;
import com.fx.sharingbikes.common.rest.BaseController;
import com.fx.sharingbikes.user.entity.LoginInfo;
import com.fx.sharingbikes.user.entity.User;
import com.fx.sharingbikes.user.entity.UserElement;
import com.fx.sharingbikes.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController extends BaseController {

    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    @PostMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<String> login(@RequestBody LoginInfo loginInfo) {
        ApiResult<String> resp = new ApiResult<>();
        try {
            String data = loginInfo.getData();
            String key = loginInfo.getKey();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(key)) {
                throw new SharingBikesException("参数校验失败");
            }
            String token = userService.login(data, key);
            resp.setData(token);
        } catch (SharingBikesException e) {
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to login", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

    @RequestMapping("modifyNickName")
    public ApiResult modifyNickName(@RequestBody User user) {
        ApiResult resp = new ApiResult();
        try {
            UserElement userElement = getCurrentUser();
            user.setId(userElement.getUserId());
            userService.modifyNickName(user);
        } catch (SharingBikesException e) {
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to login", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

    @RequestMapping("sendVercode")
    public ApiResult sendVercode(@RequestBody User user, HttpServletRequest request) {
        ApiResult resp = new ApiResult();
        try {
            userService.sendVercode(user.getMobile(), getIpFromRequest(request));
        } catch (SharingBikesException e) {
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to send sms vercode", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

    @PostMapping("uploadHeadImg")
    public ApiResult<String> uploadHeadImg(HttpServletRequest req, @RequestParam(required = false) MultipartFile file) {
        ApiResult<String> resp = new ApiResult<>();
        try {
            UserElement userElement = getCurrentUser();
            userService.uploadHeadImg(file, userElement.getUserId());
            resp.setMessage("上传成功");
        } catch (SharingBikesException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to update user info", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }
}
