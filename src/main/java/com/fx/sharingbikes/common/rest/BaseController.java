package com.fx.sharingbikes.common.rest;

import com.fx.sharingbikes.cache.CommonCacheUtil;
import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class BaseController {

    @Autowired
    private CommonCacheUtil cacheUtil;

    protected UserElement getCurrentUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.REQUEST_TOKEN_KEY);
        if (!StringUtils.isBlank(token)) {
            try {
                UserElement userElement = cacheUtil.getUserByToken(token);
                return userElement;
            } catch (Exception e) {
                log.error("Fail to get user by token", e);
                throw e;
            }
        }
        return null;
    }
}
