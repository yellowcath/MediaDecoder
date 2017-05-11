package com.hw.codecplayer.extractor;

import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by huangwei on 2017/5/9.
 */

public interface IMediaSource {
    public void setDataSource(@NonNull List<MediaData> dataList) throws IOException;

    public int readSampleData(@NonNull ByteBuffer byteBuf, int offset);

    /**
     * @return Returns the current sample's presentation time in microseconds. or -1 if no more samples are available.
     */
    public long getSampleTime();

    public boolean advance() throws TimeoutException, IOException;

    public void release();

    public MediaFormat getCurrentMediaFormat();

    public void setSeekAccuracy(long timeMs);

    public int getCurrentDataIndex();
}
