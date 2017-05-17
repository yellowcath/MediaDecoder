package com.hw.codecplayer.codec;

import android.media.MediaFormat;

/**
 * Created by huangwei on 2017/5/11.
 */

public interface IMediaDecoder {

    public void prepare() throws Exception;

    public void start();

    public void pause();

    public void resume();

    public void release();

    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener);

    public MediaFormat getCodecMediaFormat();
    public MediaFormat getExtractorMediaFormat();
}
