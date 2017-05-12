package com.hw.codecplayer.extractor;

import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by huangwei on 2017/5/9.
 */

public interface IMediaSource {
    public void setDataSource(@NonNull List<MediaData> dataList) throws Exception;

    public int readSampleData(@NonNull ByteBuffer byteBuf, int offset);

    /**
     * @return Returns the current sample's presentation time in microseconds. or -1 if no more samples are available.
     */
    public long getSampleTime();

    public boolean advance() throws Exception;

    public void release();

    public MediaFormat getCurrentMediaFormat();

    public void setSeekAccuracy(long timeMs);

    public int getCurrentDataIndex();
}
