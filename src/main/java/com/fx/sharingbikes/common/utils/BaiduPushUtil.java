package com.fx.sharingbikes.common.utils;

import com.baidu.yun.push.auth.PushKeyPair;
import com.baidu.yun.push.client.BaiduPushClient;
import com.baidu.yun.push.exception.PushClientException;
import com.baidu.yun.push.exception.PushServerException;
import com.baidu.yun.push.model.PushMsgToSingleDeviceRequest;
import com.baidu.yun.push.model.PushMsgToSingleDeviceResponse;
import com.fx.sharingbikes.common.constants.Constants;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.user.entity.UserElement;

public class BaiduPushUtil {

    public static void pushMsgToSingleDevice(UserElement userElement, String message) throws SharingBikesException {
        PushKeyPair pair = new PushKeyPair(Constants.BAIDU_YUN_PUSH_API_KEY, Constants.BAIDU_YUN_PUSH_SECRET_KEY);
        BaiduPushClient pushClient = new BaiduPushClient(pair, Constants.CHANNEL_REST_URL);
        try {
            PushMsgToSingleDeviceRequest request = new PushMsgToSingleDeviceRequest()
                    .addChannelId(userElement.getPushChannelId())
                    .addMsgExpires(3600)
                    .addMessageType(1)
                    .addMessage(message);
            if ("android".equals(userElement.getPlatform())) {
                request.setDeviceType(3);
            } else if ("ios".equals(userElement.getPlatform())) {
                request.setDeviceType(4);
            }
            PushMsgToSingleDeviceResponse response = pushClient.pushMsgToSingleDevice(request);
        } catch (PushClientException | PushServerException e) {
            e.printStackTrace();
            throw new SharingBikesException(e.getMessage());
        }
    }
}
