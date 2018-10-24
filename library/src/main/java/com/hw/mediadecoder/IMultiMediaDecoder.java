package com.hw.mediadecoder;

import android.media.MediaFormat;
import com.hw.mediadecoder.codec.OnFrameDecodeListener;

/**
 * Created by huangwei on 2017/5/11.
 */

public interface IMultiMediaDecoder {

    public void prepare() throws Exception;

    public void start();

    public void pause();

    public void resume();

    public void release();

    public void setOnFrameDecodeListener(OnFrameDecodeListener frameDecodeListener);

    public MediaFormat getCodecMediaFormat();
    public MediaFormat getExtractorMediaFormat();

    int getFrameRate();
}
