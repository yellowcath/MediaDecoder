package com.hw.codecplayer.demo.util;

import com.hw.codecplayer.util.CL;

/**
 * Created by huangwei on 2017/5/18.
 */

public class DecodeTrace {
    private static final int REFRESH_INTERVAL = 1000;
    private static long mStartTime;
    private static int mFrameCount;

    public static void onFrameDecode() {
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
            return;
        }
        long curTime = System.currentTimeMillis();
        if (curTime - mStartTime > REFRESH_INTERVAL) {
            mStartTime = curTime;
            mFrameCount = 0;
            return;
        }
        mFrameCount++;
        float second = (curTime - mStartTime) / 1000f;

        CL.i(String.format("Decode FPS:%.1f", mFrameCount / second));
    }
}
