package com.hw.codecplayer.demo;

import android.os.SystemClock;
import com.hw.codecplayer.demo.gl.GLFrameRenderer;
import com.hw.codecplayer.util.CL;

/**
 * Created by huangwei on 2017/5/6.
 */
public class PlayDemo {

    private long mStartTimeMs;
    private GLFrameRenderer frameRenderer;
    private byte[] y;
    private byte[] u;
    private byte[] v;

    public PlayDemo(GLFrameRenderer renderer) {
        frameRenderer = renderer;
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
        MyImage myImage = null;
        try {
            myImage = FrameQueue.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (myImage == null) {
            return;
        }
        if (mStartTimeMs == 0) {
            mStartTimeMs = System.currentTimeMillis();
        }
        long currentTimeMs = System.currentTimeMillis();
        CL.i("sleep","frameTimeMs:"+myImage.timeUs/1000+"ms");
        long showTimeMs = mStartTimeMs + myImage.timeUs/1000;
        if (showTimeMs > currentTimeMs) {
            CL.i("sleep","sleeptime:"+(showTimeMs-currentTimeMs)+"ms");
            SystemClock.sleep(showTimeMs-currentTimeMs);
        }
        frameRenderer.update(myImage.y, myImage.u, myImage.v);
    }
}
