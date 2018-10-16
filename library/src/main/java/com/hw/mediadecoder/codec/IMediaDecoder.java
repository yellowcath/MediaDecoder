package com.hw.mediadecoder.codec;

import android.media.MediaFormat;

import java.io.IOException;

/**
 * Created by huangwei on 2017/5/12.
 */

public interface IMediaDecoder {
    public void prepare() throws IOException,IllegalStateException;

    public void seekAndDecode();

    public boolean waitSeekFinish(int timeoutMs);

    public void loadAndSeekAsync();

    public void start();

    public void pause();

    public void resume();

    public void release();

    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener);

    MediaFormat getCodecMediaFormat();

    MediaFormat getExtractorMediaFormat();
}
