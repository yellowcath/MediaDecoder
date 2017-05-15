package com.hw.codecplayer.demo;

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

    public PlayDemo(GLFrameRenderer renderer, MediaFramePool pool) {
        frameRenderer = renderer;
        mMediaFramePool = pool;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    startImpl();
                }
            }
        }).start();
    }

    private void startImpl() {
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
        CL.i("sleep", "frameTimeMs:" + mediaFrame.timestampUs / 1000 + "ms");
        long showTimeMs = mStartTimeMs + mediaFrame.timestampUs / 1000;
        if (showTimeMs > currentTimeMs) {
            CL.i("sleep", "sleeptime:" + (showTimeMs - currentTimeMs) + "ms");
            SystemClock.sleep(showTimeMs - currentTimeMs);
        }

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
                mediaFrame.width,mediaFrame.height,
                mediaFrame.cropRect.left,mediaFrame.cropRect.top,mediaFrame.cropRect.right,mediaFrame.cropRect.bottom,
                y, u, v);
        long e = System.currentTimeMillis();
        if(CL.isLogEnable()) {
            CL.i(String.format("planesToYUV,%dX%d,takes %dms", mediaFrame.width, mediaFrame.height, e - s));
        }
        mMediaFramePool.cacheObject(mediaFrame);
        frameRenderer.update(y, u, v);
    }

    private void checkInit(MediaFrame mediaFrame) {
        int ySize = mediaFrame.cropRect.width()*mediaFrame.cropRect.height();
        if (y == null || y.capacity() < ySize) {
            y = ByteBuffer.allocateDirect(ySize);
        }
        int uvSize = ySize / 4;
        if (u == null || u.capacity() < uvSize) {
            u = ByteBuffer.allocateDirect(uvSize);
        }
        if (v == null || v.capacity() < uvSize) {
            v = ByteBuffer.allocateDirect(uvSize);
        }
    }
}
