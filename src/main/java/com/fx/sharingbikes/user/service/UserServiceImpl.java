package com.fx.sharingbikes.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fx.sharingbikes.cache.CommonCacheUtil;
import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.common.utils.QiniuFileUploadUtil;
import com.fx.sharingbikes.common.utils.RandomNumberCode;
import com.fx.sharingbikes.jms.SmsProcessor;
import com.fx.sharingbikes.security.AESUtil;
import com.fx.sharingbikes.security.Base64Util;
import com.fx.sharingbikes.security.MD5Util;
import com.fx.sharingbikes.security.RSAUtil;
import com.fx.sharingbikes.user.dao.UserMapper;
import com.fx.sharingbikes.user.entity.User;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.jms.Destination;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String SMS_QUEUE = "sms.queue";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommonCacheUtil cacheUtil;

    private static final String VERIFYCODE_PREFIX = "verify.code.";

    @Autowired
    private SmsProcessor smsProcessor;

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
            UserElement userElement = new UserElement();
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
    public void modifyNickName(User user) throws SharingBikesException {
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public void sendVercode(String mobile, String ip) throws SharingBikesException {
        String verCode = RandomNumberCode.verCoder();
        int result = cacheUtil.cacheForVerificationCode(VERIFYCODE_PREFIX + mobile, verCode, "reg", 60, ip);
        if (result == 1) {
            log.info("当前验证码未过期，请稍后重试");
            throw new SharingBikesException("当前验证码未过期，请稍后重试");
        } else if (result == 2) {
            log.info("超过当日验证码次数上线");
            throw new SharingBikesException("超过当日验证码次数上限");
        } else if (result == 3) {
            log.info("超过当日验证码次数上限 {}", ip);
            throw new SharingBikesException(ip + "超过当日验证码次数上限");
        }
        log.info("Sending verify code {} for phone {}", verCode, mobile);
        Destination destination = new ActiveMQQueue(SMS_QUEUE);
        Map<String, String> smsParam = new HashMap<>();
        smsParam.put("mobile", mobile);
        smsParam.put("tplId", Constants.MDSMS_VERCODE_TPLID);
        smsParam.put("vercode", verCode);
        String message = JSON.toJSONString(smsParam);
        smsProcessor.sendSmsToQueue(destination, message);
    }

    @Override
    public String uploadHeadImg(MultipartFile file, Long userId) throws SharingBikesException {
        try {
            User user = userMapper.selectByPrimaryKey(userId);
            String imgUrlName = QiniuFileUploadUtil.uploadHeadImg(file);
            user.setHeadImg(imgUrlName);
            userMapper.updateByPrimaryKeySelective(user);
            return Constants.QINIU_HEAD_IMG_BUCKET_URL+"/"+Constants.QINIU_HEAD_IMG_BUCKET_NAME+"/"+imgUrlName;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new SharingBikesException("头像上传失败");
        }
    }

    private String generateToken(User user) {
        String source = user.getId() + ":" + user.getMobile() + ":" + System.currentTimeMillis();
        return MD5Util.getMD5(source);
    }
}
