package com.hw.mediadecoder.util;

import android.media.Image;

/**
 * Created by huangwei on 2018/10/15 0015.
 */
public interface MediaDataAdapter<DATA> {
    public DATA adapte(Image image,int codecColorFormat, final long frameTimeUs);

    void setMediaDataPool(MediaDataPool<DATA> pool);
}
