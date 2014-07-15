package org.chrjs.wearsmoking;

import android.util.Log;

public class Logi {
    public static void Debug(String content) {
        if (BuildConfig.DEBUG) {
            Log.d("remit", content);
        }
    }

    public static void Debug(String content, long[] ids) {
        StringBuilder sb = new StringBuilder("");
        for (long id : ids) {
            sb.append(id + ",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        Debug(content + sb.toString());
    }
}
