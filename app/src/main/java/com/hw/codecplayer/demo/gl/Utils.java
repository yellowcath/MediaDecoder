package com.hw.codecplayer.demo.gl;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Created by huangwei on 2017/5/6.
 */
class Utils {

    private static final String TAG = "hwLog";
    public static void LOGD(String msg){
        Log.d("TAG",msg);
    }

    public static void LOGE(String msg){
        Log.e("TAG",msg);
    }

    public static String readStringForAssets(Context context, String name) throws IOException {
        InputStream open = context.getAssets().open(name);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(open));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while((s = bufferedReader.readLine())!=null){
            sb.append(s);
        }
        return sb.toString();
    }
}
