package com.menglong.common;

import android.util.Log;

/**
 * Created by sml on 2019/7/25.
 * 日志打印
 */

public class LogUtil {

    public static String TAG = "Sunml";

    public static void i(String msg) {
        Log.i(TAG, msg + "");
    }
}
