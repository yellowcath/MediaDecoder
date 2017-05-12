package com.hw.codecplayer.codec;

import android.media.Image;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import com.hw.codecplayer.extractor.MediaData;
import com.hw.codecplayer.extractor.SeekThread;
import com.hw.codecplayer.util.CL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2017/5/12.
 */

public class MediaDecoder implements IMediaDecoder, OnFrameDecodeListener {
    /**
     * 用于两个视频切换时两帧之间的时间间隔
     */
    private static final int DEFAULT_FRAME_INTERVAL_MS = 30;
    private static final int DEFAULT_TIME_OUT = 10000;
    /**
     * 默认seek精度
     */
    static final long DEFAULT_SEEK_ACCURACY = 50;

    private List<MediaData> mDataList;
    private MediaData mCurData;
    private int mCurIndex;
    private MediaLoader mCurLoader, mNextLoader;
    private SeekThread mCurThread, mNextThread;
    /**
     * seek时的精度，单位ms
     */
    private long mSeekAccuracyMs = DEFAULT_SEEK_ACCURACY;
    private OnFrameDecodeListener mFrameDecodeListener;
    private long mTotalTimestampUs;
    private long mPreFrameTimestampUs;

    public MediaDecoder(List<MediaData> mediaDataList) {
        mDataList = new ArrayList<>(mediaDataList);
    }

    @Override
    public void prepare() throws Exception {
        MediaData firstData = mDataList.get(0);

        //异步加载第二个
        if (mDataList.size() > 1) {
            mNextThread = new SeekThread("SeekThread2");
            mNextThread.start();
            preLoad(mDataList.get(1), mNextThread);
        }
        //同步加载第一个
        mCurThread = new SeekThread("SeekThread1");
        mCurThread.start();
        mCurLoader = new MediaLoader(new MediaExtractor(), firstData, mCurThread, mSeekAccuracyMs);
        mCurLoader.loadAndSeekAsync();
        mCurLoader.waitSeekFinish(DEFAULT_TIME_OUT);
        mCurLoader.setOnFrameDecodeListener(this);
        mCurData = firstData;
        mCurIndex = 0;

    }

    private void preLoad(@NonNull MediaData mediaData, SeekThread thread) {
        CL.i("预加载下一个视频:" + mediaData.mediaPath);
        mNextLoader = new MediaLoader(new MediaExtractor(), mediaData, thread, mSeekAccuracyMs);
        mNextLoader.loadAndSeekAsync();
    }

    @Override
    public void start() {
        mCurLoader.start();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void release() {

    }

    @Override
    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener) {
        mFrameDecodeListener = frameDecodeListener;
    }

    @Override
    public MediaFormat getCurrentMediaFormat() {
        return mCurLoader.getCurrentMediaFormat();
    }

    @Override
    public void onFrameDecode(Image frameImage, long frameTimeUs, boolean end) {
        if (!end) {
            if (mPreFrameTimestampUs == 0) {
                mPreFrameTimestampUs = frameTimeUs;
            }
            long timeAddUs = frameTimeUs - mPreFrameTimestampUs;
            mTotalTimestampUs += timeAddUs;
            mPreFrameTimestampUs = frameTimeUs;
            CL.i("onFrameDecode,timeAddMs" + timeAddUs / 1000 + " 总时长:" + mTotalTimestampUs / 1000 + "ms");
            if (mFrameDecodeListener != null) {
                mFrameDecodeListener.onFrameDecode(frameImage, mTotalTimestampUs, false);
            }
        } else {
            mPreFrameTimestampUs = 0;
            mTotalTimestampUs += DEFAULT_FRAME_INTERVAL_MS*1000;
            CL.i(mCurData.toString() + "解码结束");
            if (mCurIndex == mDataList.size() - 1) {
                CL.i("已经是最后一个片段，解码结束");
                if (mFrameDecodeListener != null) {
                    mFrameDecodeListener.onFrameDecode(null, 0, true);
                }
                return;
            }
            //等待下一个片段
            try {
                CL.w("等待下一个片段Seek");
                mNextLoader.waitSeekFinish(DEFAULT_TIME_OUT);
                CL.w("下一个片段Seek成功");
            } catch (Exception e) {
                CL.e("切换片段失败");
                CL.e(e);
                if (mFrameDecodeListener != null) {
                    mFrameDecodeListener.onDecodeError(e);
                }
                return;
            }
            //切换下一个片段
            CL.i("切换下一个片段Seek");
            mCurLoader.release();
            mCurLoader.setOnFrameDecodeListener(null);
            mCurLoader = mNextLoader;
            mCurIndex++;
            mCurData = mDataList.get(mCurIndex);
            mCurLoader.setOnFrameDecodeListener(this);
            //如果有再下一个片段，继续预加载
            if (mCurIndex < mDataList.size() - 1) {
                CL.i("预加载下一个片段");
                preLoad(mDataList.get(mCurIndex + 1), mCurThread);
            }
            //启动
            mCurThread = mNextThread;
            mCurLoader.start();
        }
    }

    @Override
    public void onDecodeError(Throwable t) {
        if (mFrameDecodeListener != null) {
            mFrameDecodeListener.onDecodeError(t);
        }
    }
}
