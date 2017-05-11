package com.hw.codecplayer.codec;

import com.hw.codecplayer.extractor.IMediaSource;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by huangwei on 2017/5/11.
 */

public interface IMediaDecoder {

    public void start() throws TimeoutException, IOException;

    public void pause();

    public void resume();

    public void release();

    public void setMediaSource(IMediaSource mediaSource);

    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener);
}
