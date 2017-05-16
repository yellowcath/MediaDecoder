package com.hw.codecplayer.demo;

import android.media.MediaCodecInfo;
import android.os.SystemClock;
import com.hw.codecplayer.demo.gl.GLFrameRenderer;
import com.hw.codecplayer.domain.MediaFrame;
import com.hw.codecplayer.util.CL;
import com.hw.codecplayer.util.MediaFramePool;
import com.hw.codecplayer.util.NativeUtil;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/6.
 */
public class PlayDemo {

    private long mStartTimeMs;
    private GLFrameRenderer frameRenderer;
    private MediaFramePool mMediaFramePool;

    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;
    private ByteBuffer uv;
    private volatile boolean mRun = true;

    public PlayDemo(GLFrameRenderer renderer, MediaFramePool pool) {
        frameRenderer = renderer;
        mMediaFramePool = pool;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRun) {
                    transform();
                }
            }
        }).start();
    }

    private void transform() {
        MediaFrame mediaFrame = null;
        try {
            mediaFrame = mMediaFramePool.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mediaFrame == null) {
            return;
        }

        if (mStartTimeMs == 0) {
            mStartTimeMs = System.currentTimeMillis();
        }
        checkInit(mediaFrame);
        long currentTimeMs = System.currentTimeMillis();
        CL.i("frameTimeMs:" + mediaFrame.timestampUs / 1000 + "ms");
        long showTimeMs = mStartTimeMs + mediaFrame.timestampUs / 1000;
        if (showTimeMs > currentTimeMs) {
            CL.i("sleeptime:" + (showTimeMs - currentTimeMs) + "ms");
            SystemClock.sleep(showTimeMs - currentTimeMs);
        }
        if (mediaFrame.useUVBuffer()) {
            transformAndUpdate420SP(mediaFrame);
        } else {
            transformAndUpdateYUV(mediaFrame);
        }
    }

    /**
     * YUV420sp的情况直接拷贝
     * @param mediaFrame
     */
    private void transformAndUpdate420SP(MediaFrame mediaFrame) {
        y.clear();
        uv.clear();
        y.put(mediaFrame.buffer1);
        uv.put(mediaFrame.buffer2);
        y.position(0);
        uv.position(0);
        mMediaFramePool.cacheObject(mediaFrame);
        frameRenderer.update(y, uv,mediaFrame.width,mediaFrame.height,mediaFrame.useUVBuffer());
    }
    /**
     * YUV420p的情况直接拷贝
     * @param mediaFrame
     */
    private void transformAndUpdate420P(MediaFrame mediaFrame){
        y.clear();
        u.clear();
        v.clear();
        y.put(mediaFrame.buffer1);
        u.put(mediaFrame.buffer2);
        v.put(mediaFrame.buffer3);
        y.position(0);
        u.position(0);
        v.position(0);
        mMediaFramePool.cacheObject(mediaFrame);
        frameRenderer.update(y, u,v,mediaFrame.width,mediaFrame.height,mediaFrame.useUVBuffer());
    }
    /**
     * 其它情况根据Image的参数转换
     * @param mediaFrame
     */
    private void transformAndUpdateYUV(MediaFrame mediaFrame) {
        if(mediaFrame.codecColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar){
            transformAndUpdate420P(mediaFrame);
            return;
        }
        CL.i("transformAndUpdateYUV");
        int capacity1 = mediaFrame.buffer1.capacity();
        int capacity2 = mediaFrame.buffer2.capacity();
        int capacity3 = mediaFrame.buffer3.capacity();

        y.clear();
        u.clear();
        v.clear();
        long s = System.currentTimeMillis();
        NativeUtil.planesToYUV(mediaFrame.buffer1, mediaFrame.buffer2, mediaFrame.buffer3,
                capacity1, capacity2, capacity3,
                mediaFrame.pixelStride1, mediaFrame.pixelStride2, mediaFrame.pixelStride3,
                mediaFrame.rowStride1, mediaFrame.rowStride2, mediaFrame.rowStride3,
                mediaFrame.width,
                y, u, v);
        long e = System.currentTimeMillis();
        if (CL.isLogEnable()) {
            CL.i(String.format("planesToYUV,%dX%d,takes %dms", mediaFrame.width, mediaFrame.height, e - s));
        }
        mMediaFramePool.cacheObject(mediaFrame);
        frameRenderer.update(y, u, v,mediaFrame.width,mediaFrame.height,mediaFrame.useUVBuffer());
    }

    private void checkInit(MediaFrame mediaFrame) {
        int ySize = mediaFrame.width * mediaFrame.height;
        if (y == null || y.capacity() < ySize) {
            y = ByteBuffer.allocateDirect(ySize);
        }
        if (mediaFrame.useUVBuffer()) {
            int uvSize = ySize / 2;
            if (uv == null || uv.capacity() < uvSize) {
                uv = ByteBuffer.allocateDirect(uvSize);
            }
        } else {
            int uvSize = ySize / 4;
            if (u == null || u.capacity() < uvSize) {
                u = ByteBuffer.allocateDirect(uvSize);
            }
            if (v == null || v.capacity() < uvSize) {
                v = ByteBuffer.allocateDirect(uvSize);
            }
        }
    }

    public void release(){
        mRun = false;
    }
}
