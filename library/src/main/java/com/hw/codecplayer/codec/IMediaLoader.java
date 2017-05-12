package com.hw.codecplayer.codec;

import android.media.MediaFormat;

import java.io.IOException;

/**
 * Created by huangwei on 2017/5/12.
 */

public interface IMediaLoader {
    public void prepare() throws IOException;

    public void seekAndDecode();

    public boolean waitSeekFinish(int timeoutMs) throws Exception;

    public void loadAndSeekAsync();

    public void start();

    public void pause();

    public void resume();

    public void release();

    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener);

    MediaFormat getCurrentMediaFormat();
}
