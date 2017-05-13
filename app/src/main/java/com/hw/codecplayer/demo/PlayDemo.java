package com.hw.codecplayer.demo;

import android.os.SystemClock;
import com.hw.codecplayer.demo.gl.GLFrameRenderer;
import com.hw.codecplayer.domain.MediaFrame;
import com.hw.codecplayer.util.CL;
import com.hw.codecplayer.util.MediaFramePool;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/6.
 */
public class PlayDemo {

    private long mStartTimeMs;
    private GLFrameRenderer frameRenderer;
    private byte[] y;
    private byte[] u;
    private byte[] v;
    private MediaFramePool mMediaFramePool;

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
        long currentTimeMs = System.currentTimeMillis();
        CL.i("sleep","frameTimeMs:"+mediaFrame.timestampUs/1000+"ms");
        long showTimeMs = mStartTimeMs + mediaFrame.timestampUs/1000;
        if (showTimeMs > currentTimeMs) {
            CL.i("sleep","sleeptime:"+(showTimeMs-currentTimeMs)+"ms");
            SystemClock.sleep(showTimeMs-currentTimeMs);
        }

        int ySize = mediaFrame.width*mediaFrame.height;
        int uvSize = ySize/4;
        if(y ==null || y.length!=ySize){
            y = new byte[ySize];
        }
        if(u ==null || u.length!=ySize){
            u = new byte[uvSize];
        }
        if(v ==null || v.length!=ySize){
            v = new byte[uvSize];
        }
        ByteBuffer YUVBuffer = mediaFrame.YUVBuffer;

        YUVBuffer.get(y);
        int uIndex = 0;
        int vIndex = 0;
        int size = ySize+uvSize*2;
        for (int i = ySize; i < size; i++) {
            if(i>=YUVBuffer.capacity()){
                CL.i("i:"+i);
                break;
            }
            if (i % 2 == 0) {
                u[uIndex++] = YUVBuffer.get(i);
            } else {
                v[vIndex++] = YUVBuffer.get(i);
            }
        }

        frameRenderer.update(y, u, v);
    }
}
