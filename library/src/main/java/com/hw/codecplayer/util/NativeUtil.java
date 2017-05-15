package com.hw.codecplayer.util;

import android.media.Image;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/14.
 */

public class NativeUtil {
    static {
        System.loadLibrary("NativeUtilJni");
    }

    public static native void planesToYUV(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3, int capacity1, int capacity2, int capacity3, int pixelStride1, int pixelStride2, int pixelStride3,int rowStrideY,int rowStrideU,int rowStrideV, int width, ByteBuffer bufferY, ByteBuffer bufferU, ByteBuffer bufferV);
}
