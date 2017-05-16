package com.hw.codecplayer.codec;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import com.hw.codecplayer.domain.MediaData;
import com.hw.codecplayer.util.CL;
import com.hw.codecplayer.util.MediaUtil;
import com.hw.codecplayer.util.RunnableThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by huangwei on 2017/5/11.
 */

public class MediaLoader implements IMediaLoader {
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
    private Exception mThrowable;
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

    public MediaLoader(MediaExtractor mediaExtractor, MediaData mediaData, RunnableThread seekThread, long seekAccuracyMs) {
        mHandlerThread = seekThread;
        mMediaExtractor = mediaExtractor;
        mSeekAccuracyMs = seekAccuracyMs;
        mSeekLatch = new CountDownLatch(1);
        mDecodeLatch = new CountDownLatch(1);
        mSeekToTimeMs = mediaData.startTimeMs;
        mMediaData = mediaData;
        mMode = Mode.UNINITED;
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
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
            int colorFormat = MediaUtil.getSupportColorForamt(mMediaCodec,mime);
            CL.i("getSupportColorForamt:"+colorFormat);
            trackFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,colorFormat);
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

    private void outputFormatChanged(){
        mOutputFormat  = mMediaCodec.getOutputFormat();
        mCodecColorFormat = mOutputFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
    }
    @Override
    public void seekAndDecode() {
        if (mSeekToTimeMs <= 0 || !mMediaData.shouldCut) {
            CL.i("不需要Seek");
            return;
        }
        mStartTime = System.currentTimeMillis();
        long seekToTimeUs = mSeekToTimeMs * 1000;
        mMediaExtractor.seekTo(seekToTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long seekSampleTimeMs = mMediaExtractor.getSampleTime() / 1000;
        CL.i("seekToMs:" + mSeekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
        if (mSeekToTimeMs - seekSampleTimeMs <= mSeekAccuracyMs) {
            CL.i("一步到位，颇费");
            mEndTime = System.currentTimeMillis();
            return;
        }
        mMediaCodec.start();
        startDecode(mMediaCodec);
    }

    private void startDecode(MediaCodec codec) {
        while (true) {
            processInput(codec);
            boolean reachEnd = processOutput(codec);
            if (reachEnd) {
                break;
            }
        }
    }

    private void processInput(MediaCodec codec) {
        int inputBufferId = codec.dequeueInputBuffer(TIME_OUT);
        if (inputBufferId < 0) {
            return;
        }
        ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferId);
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
        }
        mInputFrameCount++;
        int size = mMediaExtractor.readSampleData(byteBuffer, 0);
        if (size == -1 || sampleTimeMs > mMediaData.endTimeMs) {
            if (mMode == Mode.SEEK) {
                mThrowable = new IOException("出现异常，已经读到视频尾");
            }
            CL.i("已到视频尾");
            mMediaCodec.queueInputBuffer(inputBufferId, 0, 0, sampleTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return;
        }
        mMediaCodec.queueInputBuffer(inputBufferId, 0, size, sampleTimeUs, 0);
        if (mMode == Mode.SEEK) {
            CL.i("循环seek输入第" + mInputFrameCount + "帧,seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + sampleTimeMs);
        } else {
            CL.i("Decode输入第" + mInputFrameCount + "帧,size:" + size);
        }
        mMediaExtractor.advance();
    }

    private boolean processOutput(MediaCodec codec) {
        long s = System.currentTimeMillis();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputBufferId = codec.dequeueOutputBuffer(info, TIME_OUT);
        if(outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            outputFormatChanged();
        }
        if (outputBufferId < 0) {
            return false;
        }
        mOutputFrameCount++;
//        CL.i("Mode:" + mMode + "输出第" + mOutputFrameCount + "帧,size:" + info.size + " presentationTimeUs:" + info.presentationTimeUs + " flag:" + info.flags);
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
            CL.i("已到视频尾，解码完毕");
            if (mOnFrameDecodeListener != null) {
                mOnFrameDecodeListener.onFrameDecode(null, 0,0, true);
            }
            return true;
        }
        if (mMode == Mode.DECODE) {
            Image outputImage = mMediaCodec.getOutputImage(outputBufferId);
            if (mOnFrameDecodeListener != null && info.presentationTimeUs >= mMediaData.startTimeMs * 1000) {
                mOnFrameDecodeListener.onFrameDecode(outputImage,mCodecColorFormat, info.presentationTimeUs, false);
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
    public boolean waitSeekFinish(int timeoutMs) throws Exception {
        try {
            boolean wait = mSeekLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
            if (!wait) {
                throw new TimeoutException("wait time out");
            } else if (mThrowable != null) {
                throw mThrowable;
            }
            return wait;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void loadAndSeekAsync() {
        mThrowable = null;
        mHandlerThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    prepare();
                    CL.i("初始化MediaCodec-");
                    if (mMediaData.shouldCut) {
                        seekAndDecode();
                    }
                } catch (IOException e) {
                    mThrowable = e;
                } catch (IllegalStateException e) {
                    mThrowable = e;
                }
            }
        });
    }

    @Override
    public void start() {
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
        mMediaExtractor.release();
        mMediaCodec.release();
    }

    @Override
    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener) {
        mOnFrameDecodeListener = frameDecodeListener;
    }

    @Override
    public MediaFormat getCurrentMediaFormat() {
        return mMediaCodec.getOutputFormat();
    }

    enum Mode {
        UNINITED,
        SEEK,
        DECODE
    }

}
