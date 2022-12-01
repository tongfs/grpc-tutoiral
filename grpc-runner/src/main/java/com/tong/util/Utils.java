package com.tong.util;

public class Utils {

    /**
     * 模拟耗时任务
     */
    public static void heavyTask(int second) {
        try {
            Thread.sleep(second * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
