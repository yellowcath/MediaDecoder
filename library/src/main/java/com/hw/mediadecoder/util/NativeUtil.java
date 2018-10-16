package com.hw.mediadecoder.util;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/14.
 */

public class NativeUtil {
    static {
        System.loadLibrary("NativeUtilJni");
    }
    public static native void planesToYUV1(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3, int capacity1, int capacity2, int capacity3, int pixelStride1, int pixelStride2, int pixelStride3, int rowStrideY, int rowStrideU, int rowStrideV, int width, int height,ByteBuffer bufferY, ByteBuffer bufferU, ByteBuffer bufferV);
    public static native void planesToYUV(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3,
                                          int capacity1, int capacity2, int capacity3,
                                          int pixelStride1, int pixelStride2, int pixelStride3,
                                          int rowStrideY, int rowStrideU, int rowStrideV,
                                          int width, int height,
                                          ByteBuffer bufferYUV);

    public static native void planesToYUV420p(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3,
                                              int capacity1, int capacity2, int capacity3,
                                              int pixelStride1, int pixelStride2, int pixelStride3,
                                              int rowStrideY, int rowStrideU, int rowStrideV,
                                              int width, int height,
                                              ByteBuffer bufferY, ByteBuffer bufferU, ByteBuffer bufferV);

    public static native void planesToYUV420sp(ByteBuffer buffer1, ByteBuffer buffer2, ByteBuffer buffer3,
                                               int capacity1, int capacity2, int capacity3,
                                               int pixelStride1, int pixelStride2, int pixelStride3,
                                               int rowStrideY, int rowStrideU, int rowStrideV,
                                               int width, int height,
                                               ByteBuffer bufferY, ByteBuffer bufferUV);
}
