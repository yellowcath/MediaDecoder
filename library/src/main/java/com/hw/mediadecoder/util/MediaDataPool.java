package com.hw.mediadecoder.util;

import android.media.Image;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangwei on 2017/5/13.
 */

public class MediaDataPool<DATA> {
    /**
     * 存放待渲染的数据帧
     */
    private LinkedBlockingQueue<DATA> mAvailableQueue;
    /**
     * 作为对象池使用
     */
    private LinkedList<DATA> mCacheQueue;

    private MediaDataAdapter<DATA> mMediaDataAdapter;
    private int mCacheQueueSize;
    private int mTimeOutMs = 10000;

    public MediaDataPool(int availableQueueSize, int cacheQueueSize, MediaDataAdapter<DATA> dataAdapter) {
        mAvailableQueue = new LinkedBlockingQueue<>(availableQueueSize);
        mCacheQueue = new LinkedList<>();
        mMediaDataAdapter = dataAdapter;
        mMediaDataAdapter.setMediaDataPool(this);
        mCacheQueueSize = cacheQueueSize;
    }

    public void offer(final Image image, int codecColorFormat, final long frameTimeUs) {
        try {
            DATA data = mMediaDataAdapter.adapte(image, codecColorFormat, frameTimeUs);
            mAvailableQueue.offer(data, mTimeOutMs, TimeUnit.MILLISECONDS);
            CL.i("offer,size:" + mAvailableQueue.size());
        } catch (InterruptedException e) {
            CL.e(e);
        }
    }

    public DATA poll() throws InterruptedException {
        CL.i("poll,size:" + mAvailableQueue.size());
        DATA data = mAvailableQueue.poll(mTimeOutMs, TimeUnit.MILLISECONDS);
        return data;
    }

    public void cacheObject(DATA data) {
        CL.i("cacheObject,size:" + mCacheQueue.size());
        if (mCacheQueue.size() < mCacheQueueSize) {
            mCacheQueue.offer(data);
        }
    }

    public DATA getCachedObject() {
        CL.i("getCachedObject,size:" + mCacheQueue.size());
        return mCacheQueue.poll();
    }

    public void setTimeOutMs(int mTimeOutMs) {
        this.mTimeOutMs = mTimeOutMs;
    }
}
