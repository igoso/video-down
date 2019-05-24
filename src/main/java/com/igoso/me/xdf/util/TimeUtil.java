package com.igoso.me.xdf.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by igoso on 18-4-19.
 */
public class TimeUtil {

    /**
     * get current time in string
     * eg: 2018-04-19 14:48:08
     * @return
     */
    public static String currentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
        return sdf.format(new Date());
    }

    public static String currentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
        return sdf.format(new Date());
    }

    /**
     * get current month
     * eg: 201804
     * @return
     */
    public static String currentMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMM");
        return sdf.format(new Date());
    }
}
