package com.hw.codecplayer.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by huangwei on 2017/5/9.
 */

public interface IMediaSource {
    public void setDataSource(@NonNull List<MediaData> dataList) throws IOException;

    public int readSampleData(@NonNull ByteBuffer byteBuf, int offset);

    public long getSampleTime();

    public void seekTo(MediaExtractor extractor,long timeUs);

    public boolean advance();

    public void release();

    public MediaFormat getCurrentMediaFormat();

    public void setSeekAccuracy(long timeMs);
}
