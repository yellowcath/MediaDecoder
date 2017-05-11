package com.hw.codecplayer.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import com.hw.codecplayer.util.CL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by huangwei on 2017/5/9.
 */

public class MediaSource implements IMediaSource {
    /**
     * 默认seek精度
     */
    static final long DEFAULT_SEEK_ACCURACY = 50;

    private List<MediaData> mDataList;
    private MediaExtractor mCurExtractor, mNextExtractor;
    private MediaData mCurData;
    private MediaExtractorSeeker mExtractorSeeker;
    private SeekThread mHandlerThread;

    /**
     * seek时的精度，单位ms
     */
    private long mSeekAccuracyMs = DEFAULT_SEEK_ACCURACY;

    public MediaSource() {
        mHandlerThread = new SeekThread("SeekThread");
        mHandlerThread.start();
    }

    @Override
    public void setDataSource(@NonNull List<MediaData> dataList) throws IOException {
        if (dataList == null || dataList.size() == 0) {
            throw new NullPointerException("dataList is null or empty");
        }
        mDataList = new ArrayList<>(dataList);
        MediaData firstData = dataList.get(0);
        mCurExtractor = new MediaExtractor();
        //同步加载第一个
        loadData(mCurExtractor, firstData);
        //异步加载下一个
        if (dataList.size() > 1) {
            mNextExtractor = new MediaExtractor();
            preLoad(mNextExtractor, dataList.get(1));
        }
    }

    private void preLoad(@NonNull MediaExtractor extractor, @NonNull MediaData mediaData) {
        CL.i("预加载下一个视频:" + mediaData.mediaPath);
        mExtractorSeeker = new MediaExtractorSeeker(extractor, mHandlerThread, mSeekAccuracyMs);
        mExtractorSeeker.loadAndSeekAsync(extractor, mediaData);
    }

    @Override
    public int readSampleData(@NonNull ByteBuffer byteBuf, int offset) {
        return mCurExtractor.readSampleData(byteBuf, offset);
    }

    @Override
    public long getSampleTime() {
        return mCurExtractor.getSampleTime();
    }

    @Override
    public boolean advance() throws TimeoutException, IOException {
        boolean advance = mCurExtractor.advance();
        boolean switchToNext = false;
        if (!advance) {
            switchToNext = true;
        } else {
            long sampleTimeMs = mCurExtractor.getSampleTime() / 1000;
            long endTimeMs = mCurData.endTimeMs;
            if (sampleTimeMs > endTimeMs) {
                switchToNext = true;
            }
        }
        if (!switchToNext) {
            return true;
        }
        //是否已到最后
        int index = mDataList.indexOf(mCurData);
        if (index == mDataList.size() - 1 || mExtractorSeeker == null || mNextExtractor == null) {
            CL.i("已到最后一个视频，Finish");
            return false;
        }
        MediaData nextData = mDataList.get(index + 1);
        //等待下一个视频Seek完成
        CL.i("等待下一个视频seek");
        boolean seekSuccess = mExtractorSeeker.waitSeekFinish();
        CL.i("下一个视频seek结束,seekSuccess:" + seekSuccess);
        if (!seekSuccess) {
            throw new RuntimeException("视频预加载失败:" + nextData.toString());
        }
        mCurData = nextData;
        //交换两个Extractor
        MediaExtractor tempExtractor = mCurExtractor;
        mCurExtractor = mNextExtractor;
        mNextExtractor = tempExtractor;
        mExtractorSeeker = null;
        if (index + 1 == mDataList.size() - 1) {
            CL.i("已是最后一个视频，不再预加载");
            return true;
        }
        //预加载下一个视频
        mNextExtractor.release();
        mNextExtractor = new MediaExtractor();
        preLoad(mNextExtractor, mDataList.get(index + 2));
        return true;
    }

    @Override
    public void release() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        if (mCurExtractor != null) {
            mCurExtractor.release();
        }
        if (mNextExtractor != null) {
            mNextExtractor.release();
        }
    }

    @Override
    public MediaFormat getCurrentMediaFormat() {
        if (mCurExtractor == null) {
            return null;
        }
        return mCurExtractor.getTrackFormat(getVideoTrackIndex(mCurExtractor));
    }

    @Override
    public void setSeekAccuracy(long timeMs) {
        mSeekAccuracyMs = timeMs;
    }

    @Override
    public int getCurrentDataIndex() {
        if (mCurData == null) {
            return -1;
        }
        return mDataList.indexOf(mCurData);
    }

    public void loadData(@NonNull MediaExtractor extractor, @NonNull MediaData mediaData) throws IOException {
        CL.i("开始同步加载第一个视频:" + mediaData.mediaPath);
        mCurExtractor = extractor;
        mCurData = mediaData;
        extractor.setDataSource(mediaData.mediaPath);
        int videoTrackIndex = getVideoTrackIndex(extractor);
        extractor.selectTrack(videoTrackIndex);
        MediaFormat mediaFormat = extractor.getTrackFormat(videoTrackIndex);
        CL.i("加载成功,MediaFormat:" + mediaFormat);
        if (mediaData.shouldCut) {
            CL.i("需要Seek:" + mediaData.startTimeMs);
            MediaExtractorSeeker.seekSync(extractor, mediaData.startTimeMs * 1000, mSeekAccuracyMs);
            CL.i("Seek成功，定位到" + extractor.getSampleTime() / 1000 + "ms");
        }
    }

    public static int getVideoTrackIndex(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            //获取码流的详细格式/配置信息
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }


}
