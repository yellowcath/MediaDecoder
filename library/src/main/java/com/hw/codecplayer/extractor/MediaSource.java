package com.hw.codecplayer.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import com.hw.codecplayer.demo.util.CL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2017/5/9.
 */

public class MediaSource implements IMediaSource {
    private static final long DEFAULT_SEEK_ACCURACY = 50;

    private MediaExtractor mMediaExtractor;
    private List<MediaData> mDataList;
    private MediaFormat mCurMediaFormat;
    private MediaData mCurData;
    private long mSeekAccuracy = DEFAULT_SEEK_ACCURACY;

    public MediaSource() {
    }

    @Override
    public void setDataSource(@NonNull List<MediaData> dataList) throws IOException {
        if (dataList == null || dataList.size() == 0) {
            throw new NullPointerException("dataList is null or empty");
        }
        mDataList = new ArrayList<>(dataList);
        loadData(mMediaExtractor, dataList.get(0));
    }

    @Override
    public int readSampleData(@NonNull ByteBuffer byteBuf, int offset) {
        return 0;
    }

    @Override
    public long getSampleTime() {
        return 0;
    }

    @Override
    public void seekTo(MediaExtractor extractor, long timeUs) {
        long seekToTimeMs = timeUs/1000;
        long s = System.currentTimeMillis();
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long seekSampleTimeMs = extractor.getSampleTime() / 1000;
        CL.i("seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
        int index = 0;
        while (seekToTimeMs - seekSampleTimeMs > DEFAULT_SEEK_ACCURACY) {
            index++;
            extractor.advance();
            seekSampleTimeMs = extractor.getSampleTime() / 1000;
            CL.i("循环seek,seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
        }
        long e = System.currentTimeMillis();
        CL.i(String.format("seek成功，循环%d次，耗时:%dms,seekTo:%d,seekSampleTimeMs:%d",index,e-s,seekToTimeMs,seekSampleTimeMs));
    }

    @Override
    public boolean advance() {
        return false;
    }

    @Override
    public void release() {

    }

    @Override
    public MediaFormat getCurrentMediaFormat() {
        return mCurMediaFormat;
    }

    @Override
    public void setSeekAccuracy(long timeMs) {

    }

    public void loadData(@NonNull MediaExtractor extractor, @NonNull MediaData mediaData) throws IOException {
        if (extractor != null) {
            extractor.release();
        }
        extractor = new MediaExtractor();
        extractor.setDataSource(mediaData.mediaPath);
        int videoTrackIndex = getVideoTrackIndex(extractor);
        extractor.selectTrack(videoTrackIndex);
        mCurMediaFormat = extractor.getTrackFormat(videoTrackIndex);
        if (mediaData.shouldCut) {
            seekTo(extractor, mediaData.startTimeMs*1000);
        }
    }

    private int getVideoTrackIndex(MediaExtractor extractor) {
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
