package com.hw.mediadecoder.codec;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import com.hw.mediadecoder.domain.MediaData;
import com.hw.mediadecoder.util.CL;
import com.hw.mediadecoder.util.MediaUtil;
import com.hw.mediadecoder.util.RunnableThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by huangwei on 2017/5/11.
 */

public class MediaDecoder implements IMediaDecoder {
    private static final int TIME_OUT = 10000;
    /**
     * 未读取到帧率时的默认值
     */
    private static final int DEFAULT_FRAME_RATE = 30;

    private CountDownLatch mSeekLatch;
    private CountDownLatch mDecodeLatch;

    private RunnableThread mHandlerThread;
    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaCodec;
    private MediaData mMediaData;
    private long mSeekAccuracyMs;
    private long mSeekToTimeMs;
    private int mInputFrameCount;
    private int mOutputFrameCount;
    private long mStartTime;
    private long mEndTime;
    private int mFrameRate;
    private Mode mMode;
    private OnFrameDecodeListener mOnFrameDecodeListener;
    private int mCodecColorFormat;
    private MediaFormat mOutputFormat;
    private AtomicBoolean mDecoding = new AtomicBoolean(false);

    public MediaDecoder(MediaExtractor mediaExtractor, MediaData mediaData, RunnableThread seekThread, long seekAccuracyMs) {
        mHandlerThread = seekThread;
        mMediaExtractor = mediaExtractor;
        mSeekAccuracyMs = seekAccuracyMs;
        mSeekLatch = new CountDownLatch(1);
        mDecodeLatch = new CountDownLatch(1);
        mSeekToTimeMs = mediaData.startTimeMs;
        mMediaData = mediaData;
        mMode = Mode.UNINITED;
    }

    protected void prepareImpl() throws IOException {
        //初始化Extractor
        CL.i("初始化Extractor+");
        mMediaExtractor.setDataSource(mMediaData.mediaPath);
        int videoTrackIndex = MediaUtil.getVideoTrackIndex(mMediaExtractor);
        mMediaExtractor.selectTrack(videoTrackIndex);
        MediaFormat trackFormat = mMediaExtractor.getTrackFormat(videoTrackIndex);
        CL.i("初始化Extractor-");
        //初始化MediaCodec
        CL.i("初始化MediaCodec+");
        String mime = trackFormat.getString(MediaFormat.KEY_MIME);
        mFrameRate = trackFormat.containsKey(MediaFormat.KEY_FRAME_RATE) ?
                trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE) : DEFAULT_FRAME_RATE;

        try {
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            int colorFormat = MediaUtil.getSupportColorForamt(mMediaCodec, mime);
            CL.i("getSupportColorForamt:" + colorFormat);
            trackFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            mMediaCodec.configure(trackFormat, null, null, 0);
        } catch (MediaCodec.CodecException e) {
            CL.e(e);
            mMediaCodec = MediaUtil.configDecoder(trackFormat);
        }
        if (mMediaCodec == null) {
            throw new IllegalStateException("MediaCodec初始化失败");
        }
        outputFormatChanged();
        mMode = Mode.SEEK;
    }

    @Override
    public void prepare() {
        try {
            prepareImpl();
        } catch (Exception e) {
            CL.e(e);
            mMode = Mode.ERROR;
            if (mOnFrameDecodeListener != null) {
                mOnFrameDecodeListener.onDecodeError(e);
            }
        }
    }

    private void outputFormatChanged() {
        mOutputFormat = mMediaCodec.getOutputFormat();
        mCodecColorFormat = mOutputFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
    }

    @Override
    public void seekAndDecode() {
        if (mMode == Mode.ERROR) {
            return;
        }
        if (mSeekToTimeMs <= 0 || !mMediaData.shouldCut) {
            CL.i("不需要Seek");
            mMode = Mode.DECODE;
            mSeekLatch.countDown();
        } else {
            mStartTime = System.currentTimeMillis();
            long seekToTimeUs = mSeekToTimeMs * 1000;
            mMediaExtractor.seekTo(seekToTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            long seekSampleTimeMs = mMediaExtractor.getSampleTime() / 1000;
            CL.i("seekToMs:" + mSeekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
            if (mSeekToTimeMs - seekSampleTimeMs <= mSeekAccuracyMs) {
                CL.i("一步到位，颇费");
            }
        }
        mMediaCodec.start();
        startDecode(mMediaCodec);
    }

    private void startDecode(MediaCodec codec) {
        boolean inputFinish = false;
        mDecoding.set(true);
        try {
            while (true) {
                if (mMode == Mode.UNINITED) {
                    if (mOnFrameDecodeListener != null) {
                        mOnFrameDecodeListener.onDecodeError(new RuntimeException("release() is called!"));
                    }
                    break;
                }
                if (!inputFinish) {
                    inputFinish = processInput(codec);
                }
                if (mMode == Mode.UNINITED) {
                    if (mOnFrameDecodeListener != null) {
                        mOnFrameDecodeListener.onDecodeError(new RuntimeException("release() is called!"));
                    }
                    break;
                }
                boolean reachEnd = processOutput(codec);
                if (reachEnd) {
                    break;
                }
            }
        } finally {
            mDecoding.set(false);
        }
    }

    private boolean processInput(MediaCodec codec) {
        int inputBufferId = codec.dequeueInputBuffer(TIME_OUT);
        if (inputBufferId < 0) {
            return false;
        }
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = codec.getInputBuffer(inputBufferId);
        } catch (IllegalStateException e) {
            CL.e(e);
            return false;
        }
        long sampleTimeUs = mMediaExtractor.getSampleTime();
        long sampleTimeMs = sampleTimeUs / 1000;
        long seekToTimeMs = mSeekToTimeMs;

        boolean wait = false;
        if (mMode == Mode.SEEK && seekToTimeMs - sampleTimeMs <= mSeekAccuracyMs) {
            mEndTime = System.currentTimeMillis();
            CL.i("循环seek输入结束,耗时:" + (mEndTime - mStartTime) + "ms" + " seek帧数:" + mInputFrameCount);
            mSeekLatch.countDown();
            mMode = Mode.DECODE;
            wait = true;
        } else if (mMode == Mode.DECODE) {
            wait = true;
        }
        if (wait) {
            try {
                mDecodeLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mMode == Mode.UNINITED) {
                return false;
            }
        }
        mInputFrameCount++;
        int size = mMediaExtractor.readSampleData(byteBuffer, 0);
        if (size == -1 || (mMediaData.shouldCut && mMediaData.endTimeMs != MediaData.END_TIME_VIDEO_END && sampleTimeMs > mMediaData.endTimeMs)) {
            if (mMode == Mode.SEEK) {
                if (mOnFrameDecodeListener != null) {
                    mOnFrameDecodeListener.onDecodeError(new IOException("出现异常，已经读到视频尾"));
                    mMode = Mode.ERROR;
                    return true;
                }
            }
            CL.i("已到视频尾");
            mMediaCodec.queueInputBuffer(inputBufferId, 0, 0, sampleTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return true;
        }
        mMediaCodec.queueInputBuffer(inputBufferId, 0, size, sampleTimeUs, 0);
        if (mMode == Mode.SEEK) {
            CL.i("循环seek输入第" + mInputFrameCount + "帧,seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + sampleTimeMs);
        } else {
            CL.i("Decode输入第" + mInputFrameCount + "帧,size:" + size + " timeMs:" + sampleTimeMs);
        }
        mMediaExtractor.advance();
        return false;
    }

    private boolean processOutput(MediaCodec codec) {
        long s = System.currentTimeMillis();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputBufferId = codec.dequeueOutputBuffer(info, TIME_OUT);
        if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            outputFormatChanged();
        }
        if (outputBufferId < 0) {
            return false;
        }
        mOutputFrameCount++;
        CL.i("Mode:" + mMode + "输出第" + mOutputFrameCount + "帧,size:" + info.size + " presentationTimeUs:" + info.presentationTimeUs + " flag:" + info.flags);
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
            CL.i("已到视频尾，解码完毕");
            if (mOnFrameDecodeListener != null) {
                mOnFrameDecodeListener.onFrameDecode(null, 0, 0, true);
            }
            return true;
        }
        if (mMode == Mode.DECODE) {
            Image outputImage = mMediaCodec.getOutputImage(outputBufferId);
            synchronized (this) {
                if (mOnFrameDecodeListener != null && info.presentationTimeUs >= mMediaData.startTimeMs * 1000) {
                    mOnFrameDecodeListener.onFrameDecode(outputImage, mCodecColorFormat, info.presentationTimeUs, false);
                }
            }
        } else {
            mMediaCodec.getOutputBuffer(outputBufferId);
        }
        mMediaCodec.releaseOutputBuffer(outputBufferId, false);
        long e = System.currentTimeMillis();
        CL.i("processOutput:" + (e - s) + "ms");
        return false;
    }

    @Override
    public boolean waitSeekFinish(int timeoutMs) {
        if (mMode == Mode.ERROR) {
            return false;
        }
        try {
            boolean wait = mSeekLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
            if (!wait) {
                if (mOnFrameDecodeListener != null) {
                    mOnFrameDecodeListener.onDecodeError(new TimeoutException("wait time out"));
                }
            }
            return wait;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void loadAndSeekAsync() {
        mHandlerThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                prepare();
                CL.i("初始化MediaCodec-");
                seekAndDecode();
            }
        });
    }

    @Override
    public void start() {
        CL.i("开始解码，DecodeLatch.countDown");
        mDecodeLatch.countDown();
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void release() {
        if (mDecodeLatch.getCount() == 1) {
            mDecodeLatch.countDown();
        }
        while (mDecoding.get()) {
            CL.w("waiting decode finish!");
        }
        CL.w("decode finish,start release");
        try {
            if (mMediaExtractor != null) {
                mMediaExtractor.release();
            }
            if (mMediaCodec != null) {
                mMediaCodec.release();
            }
            mMode = Mode.UNINITED;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener) {
        mOnFrameDecodeListener = frameDecodeListener;
    }

    @Override
    public MediaFormat getCodecMediaFormat() {
        return mMediaCodec.getOutputFormat();
    }

    @Override
    public MediaFormat getExtractorMediaFormat() {
        return mMediaExtractor.getTrackFormat(MediaUtil.getVideoTrackIndex(mMediaExtractor));
    }

    @Override
    public int getFrameRate() {
        return mFrameRate;
    }

    enum Mode {
        UNINITED,
        SEEK,
        DECODE,
        ERROR
    }

}
