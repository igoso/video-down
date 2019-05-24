package com.igoso.me.xdf.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的操作混淆
 * created by igoso at 2018/10/24
 **/
public class ConfuseUtil {


    /**
     * 随机睡眠 5s左右
     */
    public static void randSleep() {
        AtomicInteger bound = new AtomicInteger(5000);
        int seconds = new Random().nextInt(bound.get()) + 50;
        try {
            System.out.println("sleep " + seconds);
            TimeUnit.MILLISECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
