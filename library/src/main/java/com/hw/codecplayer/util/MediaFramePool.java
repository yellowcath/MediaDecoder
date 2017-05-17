package com.hw.codecplayer.util;

import com.hw.codecplayer.domain.MediaFrame;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangwei on 2017/5/13.
 */

public class MediaFramePool {
    /**
     * 存放待渲染的数据帧
     */
    private LinkedBlockingQueue<MediaFrame> availableQueue;
    /**
     * 作为对象池使用
     */
    private LinkedBlockingQueue<MediaFrame> cacheQueue;
    private RunnableThread mThread;
    private int mTimeOutMs = 10000;

    public MediaFramePool(int availableQueueSize, int cacheQueueSize){
        availableQueue = new LinkedBlockingQueue<>(availableQueueSize);
        cacheQueue = new LinkedBlockingQueue<>(cacheQueueSize);
        mThread = new RunnableThread("MediaFramePoolThread");
        mThread.start();
    }

    public void offer(MediaFrame mediaFrame){
        try {
            CL.i("offer,size:" + availableQueue.size());
            availableQueue.offer(mediaFrame,mTimeOutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            CL.e(e);
        }
    }

    public MediaFrame poll() throws InterruptedException {
        CL.i( "poll,size:" + availableQueue.size());
        MediaFrame mediaFrame = availableQueue.poll(mTimeOutMs, TimeUnit.MILLISECONDS);
        return mediaFrame;
    }

    public void cacheObject(MediaFrame mediaFrame){
        CL.i( "cacheObject,size:" + cacheQueue.size());
        cacheQueue.offer(mediaFrame);
    }

    public MediaFrame getCachedObject(){
        CL.i( "getCachedObject,size:" + cacheQueue.size());
        return cacheQueue.poll();
    }

    public void setTimeOutMs(int mTimeOutMs) {
        this.mTimeOutMs = mTimeOutMs;
    }
}
