package com.hw.mediadecoder;

import android.media.Image;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import com.hw.mediadecoder.codec.IMediaDecoder;
import com.hw.mediadecoder.codec.MediaDecoder;
import com.hw.mediadecoder.codec.OnFrameDecodeListener;
import com.hw.mediadecoder.domain.MediaData;
import com.hw.mediadecoder.util.RunnableThread;
import com.hw.mediadecoder.util.CL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2017/5/12.
 */

public class MultiMediaDecoder implements IMultiMediaDecoder, OnFrameDecodeListener {
    /**
     * 用于两个视频切换时两帧之间的时间间隔
     */
    private static final int DEFAULT_FRAME_INTERVAL_MS = 30;
    private static final int DEFAULT_TIME_OUT = 10 * 1000;
    /**
     * 默认seek精度
     */
    static final long DEFAULT_SEEK_ACCURACY = 50;

    private List<MediaData> mDataList;
    private MediaData mCurData;
    private int mCurIndex;
    private IMediaDecoder mCurLoader, mNextLoader;
    private RunnableThread mCurThread, mNextThread;
    /**
     * seek时的精度，单位ms
     */
    private long mSeekAccuracyMs = DEFAULT_SEEK_ACCURACY;
    private OnFrameDecodeListener mFrameDecodeListener;
    private long mTotalTimestampUs;
    private long mPreFrameTimestampUs = -1;
    private boolean mLoop;

    public MultiMediaDecoder(List<MediaData> mediaDataList) {
        if (mediaDataList == null || mediaDataList.size() == 0) {
            throw new IllegalArgumentException("mediaDataList is null or empty!");
        }
        mDataList = new ArrayList<>(mediaDataList);
    }

    @Override
    public void prepare() {
        MediaData firstData = mDataList.get(0);

        //异步加载第二个
        if (mDataList.size() > 1 || mLoop) {
            mNextThread = new RunnableThread("SeekThread2");
            mNextThread.start();
            MediaData nextData = mDataList.size() > 1 ? mDataList.get(1) : mDataList.get(0);
            preLoad(nextData, mNextThread);
        }
        //同步加载第一个
        mCurThread = new RunnableThread("SeekThread1");
        mCurThread.start();
        mCurLoader = new MediaDecoder(new MediaExtractor(), firstData, mCurThread, mSeekAccuracyMs);
        mCurLoader.setOnFrameDecodeListener(this);
        mCurLoader.loadAndSeekAsync();
        mCurLoader.waitSeekFinish(DEFAULT_TIME_OUT);
        mCurData = firstData;
        mCurIndex = 0;

    }

    private void preLoad(MediaData mediaData, RunnableThread thread) {
        CL.i("预加载下一个视频:" + mediaData.mediaPath);
        mNextLoader = new MediaDecoder(new MediaExtractor(), mediaData, thread, mSeekAccuracyMs);
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
        if (mCurLoader != null) {
            mCurLoader.release();
            mCurLoader = null;
        }
        if (mNextLoader != null) {
            mNextLoader.release();
            mNextLoader = null;
        }
        if (mCurThread != null) {
            mCurThread.quit();
            mCurThread = null;
        }
        if (mNextThread != null) {
            mNextThread.quit();
            mNextThread = null;
        }
    }

    @Override
    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener) {
        mFrameDecodeListener = frameDecodeListener;
    }

    @Override
    public MediaFormat getCodecMediaFormat() {
        return mCurLoader == null ? null : mCurLoader.getCodecMediaFormat();
    }

    @Override
    public MediaFormat getExtractorMediaFormat() {
        return mCurLoader.getExtractorMediaFormat();
    }

    @Override
    public int getFrameRate() {
        return mCurLoader == null ? 0 : mCurLoader.getFrameRate();
    }

    @Override
    public void onFrameDecode(Image frameImage, int codecColorFormat, long frameTimeUs, boolean end) {
        if (!end) {
            if (mPreFrameTimestampUs == -1) {
                mPreFrameTimestampUs = frameTimeUs;
            }
            long timeAddUs = frameTimeUs - mPreFrameTimestampUs;
            mTotalTimestampUs += timeAddUs;
            mPreFrameTimestampUs = frameTimeUs;
            CL.i("onFrameDecode,timeAddMs" + timeAddUs / 1000 + " 总时长:" + mTotalTimestampUs / 1000 + "ms");
            if (mFrameDecodeListener != null) {
                mFrameDecodeListener.onFrameDecode(frameImage, codecColorFormat, mTotalTimestampUs, false);
            }
        } else {
            mPreFrameTimestampUs = -1;
            mTotalTimestampUs += DEFAULT_FRAME_INTERVAL_MS * 1000;
            CL.i(mCurData.toString() + "解码结束");
            int nextIndex = getNextIndex(mCurIndex);
            if (nextIndex == -1) {
                CL.i("已经是最后一个片段，解码结束");
                if (mFrameDecodeListener != null) {
                    mFrameDecodeListener.onFrameDecode(null, 0, 0, true);
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
            mCurIndex = nextIndex;
            mCurData = mDataList.get(mCurIndex);
            mCurLoader.setOnFrameDecodeListener(this);

            RunnableThread tempThread = mCurThread;
            mCurThread = mNextThread;
            mNextThread = tempThread;
            //如果有再下一个片段，继续预加载
            nextIndex = getNextIndex(mCurIndex);
            if (nextIndex != -1) {
                CL.i("预加载下一个片段");
//                mCurThread.quit();
//                mCurThread = new RunnableThread("RunnableThreadNew");
//                mCurThread.start();
                preLoad(mDataList.get(nextIndex), mNextThread);
            }
            //启动
            mCurLoader.start();
        }
    }

    /**
     * @return -1代表没有下一个了
     */
    private int getNextIndex(int curIndex) {
        if (curIndex < mDataList.size() - 1) {
            return curIndex + 1;
        } else if (mLoop) {
            return 0;
        }
        return -1;
    }

    @Override
    public void onDecodeError(Throwable t) {
        if (mFrameDecodeListener != null) {
            mFrameDecodeListener.onDecodeError(t);
        }
    }

    public void setLoop(boolean loop) {
        mLoop = loop;
    }
}
