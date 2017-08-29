package com.fx.sharingbikes.sms;

public interface SmsSender {
    void sendSms(String phone, String tplId, String params);
}
