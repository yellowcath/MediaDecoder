package com.hw.codecplayer.codec;

/**
 * Created by huangwei on 2017/5/11.
 * 在荣耀6上有bug（Seek状态输入时，一直无输出）
 * 怀疑是异步模式在部分升级上来的5.0机型上支持不好
 */
public class MediaLoaderAsync {
}
//public class MediaLoaderAsync extends MediaCodec.Callback implements IMediaLoader {
//    /**
//     * 未读取到帧率时的默认值
//     */
//    private static final int DEFAULT_FRAME_RATE = 30;
//
//    private CountDownLatch mSeekLatch;
//    private CountDownLatch mDecodeLatch;
//
//    private RunnableThread mHandlerThread;
//    private MediaExtractor mMediaExtractor;
//    private MediaCodec mMediaCodec;
//    private MediaData mMediaData;
//    private Exception mThrowable;
//    private long mSeekAccuracyMs;
//    private long mSeekToTimeMs;
//    private int mInputFrameCount;
//    private int mOutputFrameCount;
//    private long mStartTime;
//    private long mEndTime;
//    private int mFrameRate;
//    private Mode mMode;
//    private OnFrameDecodeListener mOnFrameDecodeListener;
//
//    public MediaLoaderAsync(MediaExtractor mediaExtractor, MediaData mediaData, RunnableThread seekThread, long seekAccuracyMs) {
//        mHandlerThread = seekThread;
//        mMediaExtractor = mediaExtractor;
//        mSeekAccuracyMs = seekAccuracyMs;
//        mSeekLatch = new CountDownLatch(1);
//        mDecodeLatch = new CountDownLatch(1);
//        mSeekToTimeMs = mediaData.startTimeMs;
//        mMediaData = mediaData;
//        mMode = Mode.UNINITED;
//    }
//
//    @Override
//    public void prepare() throws IOException {
//        //初始化Extractor
//        CL.i("初始化Extractor+");
//        mMediaExtractor.setDataSource(mMediaData.mediaPath);
//        int videoTrackIndex = MediaUtil.getVideoTrackIndex(mMediaExtractor);
//        mMediaExtractor.selectTrack(videoTrackIndex);
//        MediaFormat trackFormat = mMediaExtractor.getTrackFormat(videoTrackIndex);
//        CL.i("初始化Extractor-");
//        //初始化MediaCodec
//        CL.i("初始化MediaCodec+");
//        String mime = trackFormat.getString(MediaFormat.KEY_MIME);
//        mFrameRate = trackFormat.containsKey(MediaFormat.KEY_FRAME_RATE) ?
//                trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE) : DEFAULT_FRAME_RATE;
//
//        mMediaCodec = MediaCodec.createDecoderByType(mime);
//        trackFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
//        mMediaCodec.setCallback(this);
//        mMediaCodec.configure(trackFormat, null, null, 0);
//        mMode = Mode.SEEK;
//    }
//
//    @Override
//    public void seekAndDecode() {
//        if (mSeekToTimeMs <= 0 || !mMediaData.shouldCut) {
//            CL.i("不需要Seek");
//            return;
//        }
//        mStartTime = System.currentTimeMillis();
//        long seekToTimeUs = mSeekToTimeMs * 1000;
//        mMediaExtractor.seekTo(seekToTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
//        long seekSampleTimeMs = mMediaExtractor.getSampleTime() / 1000;
//        CL.i("seekToMs:" + mSeekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
//        if (mSeekToTimeMs - seekSampleTimeMs <= mSeekAccuracyMs) {
//            CL.i("一步到位，颇费");
//            mEndTime = System.currentTimeMillis();
//            return;
//        }
//        mMediaCodec.start();
//    }
//
//    @Override
//    public boolean waitSeekFinish(int timeoutMs) throws Exception {
//        try {
//            boolean wait = mSeekLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
//            if (!wait) {
//                throw new TimeoutException("wait time out");
//            } else if (mThrowable != null) {
//                throw mThrowable;
//            }
//            return wait;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @Override
//    public void loadAndSeekAsync() {
//        mThrowable = null;
//        mHandlerThread.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    prepare();
//                    CL.i("初始化MediaCodec-");
//                    if (mMediaData.shouldCut) {
//                        seekAndDecode();
//                    }
//                } catch (IOException e) {
//                    mThrowable = e;
//                }
//            }
//        });
//    }
//
//    @Override
//    public void start() {
//        mDecodeLatch.countDown();
//    }
//
//    @Override
//    public void pause() {
//
//    }
//
//    @Override
//    public void resume() {
//
//    }
//
//    @Override
//    public void release() {
//
//    }
//
//    @Override
//    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener) {
//        mOnFrameDecodeListener = frameDecodeListener;
//    }
//
//    @Override
//    public void onInputBufferAvailable(MediaCodec codec, int inputBufferId) {
//        ByteBuffer byteBuffer = codec.getInputBuffer(inputBufferId);
//        long sampleTimeUs = mMediaExtractor.getSampleTime();
//        long sampleTimeMs = sampleTimeUs / 1000;
//        long seekToTimeMs = mSeekToTimeMs;
//
//        boolean wait = false;
//        if (mMode == Mode.SEEK && seekToTimeMs - sampleTimeMs <= mSeekAccuracyMs) {
//            mEndTime = System.currentTimeMillis();
//            CL.i("循环seek输入结束,耗时:" + (mEndTime - mStartTime) + "ms" + " seek帧数:" + mInputFrameCount);
//            mSeekLatch.countDown();
//            mMode = Mode.DECODE;
//            wait = true;
//        } else if (mMode == Mode.DECODE) {
//            wait = true;
//        }
//        if (wait) {
//            try {
//                mDecodeLatch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        mInputFrameCount++;
//        mMediaExtractor.advance();
//        int size = mMediaExtractor.readSampleData(byteBuffer, 0);
//        if (size == -1) {
//            if (mMode == Mode.SEEK) {
//                mThrowable = new IOException("出现异常，已经读到视频尾");
//            }
//            CL.i("已到视频尾");
//            mMediaCodec.queueInputBuffer(inputBufferId, 0, 0, sampleTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//            return;
//        }
//        mMediaCodec.queueInputBuffer(inputBufferId, 0, size, sampleTimeUs, 0);
//        if (mMode == Mode.SEEK) {
//            CL.i("循环seek输入第"+mInputFrameCount+"帧,seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + sampleTimeMs);
//        } else {
//            CL.i("Decode输入第"+mInputFrameCount+"帧,size:" + size);
//        }
//    }
//
//    @Override
//    public void onOutputBufferAvailable(MediaCodec codec, int outputBufferId, MediaCodec.BufferInfo info) {
//        mOutputFrameCount++;
//        CL.i("Mode:"+mMode+"输出第" + mOutputFrameCount + "帧,size:" + info.size + " presentationTimeUs:" + info.presentationTimeUs+" flag:"+info.flags);
//        if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM ){
//            CL.i("已到视频尾，解码完毕");
//            if (mOnFrameDecodeListener != null) {
//                mOnFrameDecodeListener.onFrameDecode(null, 0,true);
//            }
//            return;
//        }
//        if (mMode == Mode.DECODE) {
//            Image outputImage = mMediaCodec.getOutputImage(outputBufferId);
//            if (mOnFrameDecodeListener != null) {
//                mOnFrameDecodeListener.onFrameDecode(outputImage, info.presentationTimeUs,false);
//            }
//        }else{
//            mMediaCodec.getOutputBuffer(outputBufferId);
//        }
//        mMediaCodec.releaseOutputBuffer(outputBufferId, false);
//    }
//
//    @Override
//    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
//        mThrowable = e;
//        codec.release();
//    }
//
//    @Override
//    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
//
//    }
//    @Override
//    public MediaFormat getCurrentMediaFormat() {
//        int videoTrackIndex = MediaUtil.getVideoTrackIndex(mMediaExtractor);
//        return mMediaExtractor.getTrackFormat(videoTrackIndex);
//    }
//    enum Mode {
//        UNINITED,
//        SEEK,
//        DECODE
//    }

//}
