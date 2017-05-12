package com.hw.codecplayer.codec;

import android.media.Image;

/**
 * Created by huangwei on 2017/5/11.
 */

public interface OnFrameDecodeListener {
    void onFrameDecode(Image frameImage,long frameTimeUs,boolean end);
    void onDecodeError(Throwable t);
}
