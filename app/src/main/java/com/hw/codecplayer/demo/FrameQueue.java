package com.hw.codecplayer.demo;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangwei on 2017/5/6.
 */
public class FrameQueue {
    private static LinkedBlockingQueue<MyImage> queue = new LinkedBlockingQueue<>(10);

    public static void preparePool() {

    }

    public static MyImage poll() throws InterruptedException {
        Log.i("FrameQueue", "poll+");
        MyImage myImage = queue.poll(10, TimeUnit.SECONDS);
        Log.i("FrameQueue", "poll-,size:" + queue.size());
        return myImage;
    }

    public static void offer(MyImage image) throws InterruptedException {
        Log.i("FrameQueue", "offer+");
        queue.offer(image, 10, TimeUnit.MINUTES);
        Log.i("FrameQueue", "offer-,size:" + queue.size());
    }

}
