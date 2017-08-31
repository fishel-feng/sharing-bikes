package com.fx.sharingbikes.common.utils;

import java.util.Date;

public class DateUtil {

    public static Long getBetweenMin(Date endDate, Date nowDate) {
        long minTime = 60 * 1000;
        long diff = endDate.getTime() - nowDate.getTime();
        long min = diff / minTime;
        return min;
    }
}
