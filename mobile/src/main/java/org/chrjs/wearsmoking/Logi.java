package org.chrjs.wearsmoking;

import android.util.Log;

public class Logi {
    public static void Debug(String content) {
        if (BuildConfig.DEBUG) {
            Log.d("wearsmoking", content);
        }
    }
}
