package com.fx.sharingbikes.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fx.sharingbikes.cache.CommonCacheUtil;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.security.AESUtil;
import com.fx.sharingbikes.security.Base64Util;
import com.fx.sharingbikes.security.MD5Util;
import com.fx.sharingbikes.security.RSAUtil;
import com.fx.sharingbikes.user.dao.UserMapper;
import com.fx.sharingbikes.user.entity.User;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommonCacheUtil cacheUtil;

    @Override
    public String login(String data, String key) throws SharingBikesException {
        String token = null;
        String decryptData = null;
        try {
            byte[] aesKey = RSAUtil.decryptByPrivateKey(Base64Util.decode(key));
            decryptData = AESUtil.decrypt(data, new String(aesKey, "UTF-8"));
            if (decryptData == null) {
                throw new Exception();
            }
            JSONObject jsonObject = JSON.parseObject(decryptData);
            String mobile = jsonObject.getString("mobile");
            String code = jsonObject.getString("code");
            String platform = jsonObject.getString("platform");
            if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
                throw new Exception();
            }
            String verCode = cacheUtil.getCacheValue(mobile);
            User user;
            if (code.equals(verCode)) {
                user = userMapper.selectByMobile(mobile);
                if (user == null) {
                    user = new User();
                    user.setMobile(mobile);
                    user.setNickname(mobile);
                    userMapper.insertSelective(user);
                }
            } else {
                throw new SharingBikesException("手机号验证码不匹配");
            }
            try {
                token = generateToken(user);
            } catch (Exception e) {
                throw new SharingBikesException("生成token失败");
            }
            UserElement userElement=new UserElement();
            userElement.setMobile(mobile);
            userElement.setUserId(user.getId());
            userElement.setToken(token);
            userElement.setPlatform(platform);
            cacheUtil.putTokenWhenLogin(userElement);
        } catch (Exception e) {
            log.error("Fail to decrypt data", e);
            throw new SharingBikesException("数据解析错误");
        }
        return token;
    }

    @Override
    public void modifyNickName(User user) throws SharingBikesException{
        userMapper.updateByPrimaryKeySelective(user);
    }

    private String generateToken(User user) {
        String source = user.getId() + ":" + user.getMobile() + ":" + System.currentTimeMillis();
        return MD5Util.getMD5(source);
    }
}
