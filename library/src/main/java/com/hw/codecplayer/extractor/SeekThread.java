package com.hw.codecplayer.extractor;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangwei on 2017/5/11.
 */

public class SeekThread extends HandlerThread {

    private Handler mHandler;
    private List<Runnable> mRunnableList = new LinkedList<>();

    public SeekThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        super.run();
        synchronized (this) {
            mRunnableList.clear();
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        synchronized (this) {
            mHandler = new Handler(getLooper());
            if (mRunnableList.size() > 0) {
                for (Runnable r : mRunnableList) {
                    mHandler.post(r);
                }
                mRunnableList.clear();
            }
        }
    }

    public synchronized void postRunnable(Runnable r) {
        if (mHandler == null) {
            mRunnableList.add(r);
        } else {
            mHandler.post(r);
        }
    }

}
