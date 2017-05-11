package com.hw.codecplayer.codec;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import com.hw.codecplayer.extractor.IMediaSource;
import com.hw.codecplayer.util.CL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

/**
 * Created by huangwei on 2017/5/11.
 */

public class MediaDecoder implements IMediaDecoder {
    private static final int DEFAULT_TIMEOUT = 10000;

    private IMediaSource mMediaSource;
    private OnFrameDecodeListener mOnFrameDecodeListener;
    private MediaCodec mMediaCodec;
    private int mFrameRate;
    private volatile boolean mPause = false;

    public MediaDecoder() {

    }

    public void prepare() throws IOException {
        MediaFormat currentMediaFormat = mMediaSource.getCurrentMediaFormat();
        String mime = currentMediaFormat.getString(MediaFormat.KEY_MIME);
        mFrameRate = currentMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);

        mMediaCodec = MediaCodec.createDecoderByType(mime);
        currentMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mMediaCodec.configure(currentMediaFormat, null, null, 0);
    }

    @Override
    public void start() throws TimeoutException, IOException {
        mMediaCodec.start();
        int curMediaIndex = mMediaSource.getCurrentDataIndex();
        //上一帧的时间戳
        long preTimeStampUs = mMediaSource.getSampleTime();
        long currentTotalTimeStampUs = 0;
        long defaultFrameDuration = 1000 * 1000 / mFrameRate;
        int inputFrameIndex = 0;
        int outputFrameIndex = 0;
        while (true) {
            synchronized (this) {
                while (mPause) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            int inputBufferId = mMediaCodec.dequeueInputBuffer(DEFAULT_TIMEOUT);
            if (inputBufferId >= 0) {
                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferId);
                int size = mMediaSource.readSampleData(inputBuffer, 0);
                CL.i("readSampleData:" + size);
                if (size == -1) {
                    break;
                }
                int mediaIndex = mMediaSource.getCurrentDataIndex();
                long currentTimeStampUs = mMediaSource.getSampleTime();
                long timeAddUs;
                if (mediaIndex == curMediaIndex) {
                    timeAddUs = currentTimeStampUs - preTimeStampUs;
                    preTimeStampUs = currentTimeStampUs;
                } else {
                    //已经跳到下一个视频
                    curMediaIndex = mediaIndex;
                    timeAddUs = defaultFrameDuration;
                    preTimeStampUs = currentTimeStampUs;
                }
                currentTotalTimeStampUs += timeAddUs;
                inputFrameIndex++;
                CL.i(String.format("输入第%d帧,当前计算出来的时间戳为:%d", inputFrameIndex, currentTotalTimeStampUs));
                mMediaCodec.queueInputBuffer(inputBufferId, 0, size, currentTotalTimeStampUs, 0);
                boolean advance = mMediaSource.advance();
                if (!advance) {
                    CL.i("advancefalse，可能出错或者已经结束");
                    continue;
                }
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferId = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT);
            if (outputBufferId >= 0) {
                outputFrameIndex++;
                Image image = mMediaCodec.getOutputImage(outputBufferId);
                if (mOnFrameDecodeListener != null) {
                    mOnFrameDecodeListener.onFrameDecode(image, bufferInfo.presentationTimeUs);
                }
                CL.i("输出第" + outputFrameIndex + "帧,size:" + bufferInfo.size + " presentationTimeUs:" + bufferInfo.presentationTimeUs);
                mMediaCodec.releaseOutputBuffer(outputBufferId, false);
            }
        }
    }

    @Override
    public void pause() {
        mPause = true;
    }

    @Override
    public void resume() {
        synchronized (this) {
            mPause = false;
            notify();
        }
    }

    @Override
    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
        }
        if (mMediaSource != null) {
            mMediaSource.release();
        }
    }

    @Override
    public void setMediaSource(IMediaSource mediaSource) {
        mMediaSource = mediaSource;
    }

    @Override
    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener) {
        mOnFrameDecodeListener = frameDecodeListener;
    }
}
