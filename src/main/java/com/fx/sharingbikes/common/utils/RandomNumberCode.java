package com.fx.sharingbikes.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class RandomNumberCode {

    public static String verCoder() {
        Random random = new Random();
        return StringUtils.substring(String.valueOf(random.nextInt()), 2, 6);
    }

    public static String randomNo() {
        Random random = new Random();
        return String.valueOf(Math.abs(random.nextInt() * -10));
    }
}
