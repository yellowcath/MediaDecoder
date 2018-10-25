package com.hw.mediadecoder.util;

import android.media.Image;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangwei on 2017/5/13.
 */

public class MediaDataPool<DATA> {
    public static boolean POOL_LOG = false;
    /**
     * 存放待渲染的数据帧
     */
    private LinkedBlockingQueue<DATA> mAvailableQueue;
    /**
     * 作为对象池使用
     */
    private ConcurrentLinkedQueue<DATA> mCacheQueue;

    private MediaDataAdapter<DATA> mMediaDataAdapter;
    private int mCacheQueueSize;
    private int mTimeOutMs = 1000;

    public MediaDataPool(int availableQueueSize, int cacheQueueSize, MediaDataAdapter<DATA> dataAdapter) {
        mAvailableQueue = new LinkedBlockingQueue<>(availableQueueSize);
        mCacheQueue = new ConcurrentLinkedQueue<>();
        mMediaDataAdapter = dataAdapter;
        mMediaDataAdapter.setMediaDataPool(this);
        mCacheQueueSize = cacheQueueSize;
    }

    public void offer(final Image image, int codecColorFormat, final long frameTimeUs) {
        try {
            DATA data = mMediaDataAdapter.adapte(image, codecColorFormat, frameTimeUs);
            mAvailableQueue.offer(data, mTimeOutMs, TimeUnit.MILLISECONDS);
            if (POOL_LOG) {
                CL.i("offer,size:" + mAvailableQueue.size());
            }
        } catch (InterruptedException e) {
            CL.e(e);
        }
    }

    public DATA poll() throws InterruptedException {
        if (POOL_LOG) {
            CL.i("poll,size:" + mAvailableQueue.size());
        }
        DATA data = mAvailableQueue.isEmpty() ? null : mAvailableQueue.poll(mTimeOutMs, TimeUnit.MILLISECONDS);
        return data;
    }

    public void cacheObject(DATA data) {
        if (POOL_LOG) {
            CL.i("cacheObject,size:" + mCacheQueue.size());
        }
        if (mCacheQueue.size() < mCacheQueueSize) {
            mCacheQueue.offer(data);
        }
    }

    public DATA getCachedObject() {
        if (POOL_LOG) {
            CL.i("getCachedObject,size:" + mCacheQueue.size());
        }
        return mCacheQueue.isEmpty() ? null : mCacheQueue.poll();
    }

    public void setTimeOutMs(int mTimeOutMs) {
        this.mTimeOutMs = mTimeOutMs;
    }
}
