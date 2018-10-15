package com.hw.mediadecoder.util;

import android.media.Image;
import com.hw.mediadecoder.domain.MediaFrame;

/**
 * Created by huangwei on 2018/10/15 0015.
 */
public class MediaFrameAdapter implements MediaDataAdapter<MediaFrame> {

    protected MediaDataPool<MediaFrame> mPool;

    @Override
    public MediaFrame adapte(Image image, int codecColorFormat, final long frameTimeUs) {
        MediaFrame cachedObject = mPool.getCachedObject();
        if (cachedObject != null) {
            return MediaFrame.resetFromImage(image, codecColorFormat, frameTimeUs, cachedObject);
        } else {
            return MediaFrame.createFromImage(image, codecColorFormat, frameTimeUs);
        }
    }

    @Override
    public void setMediaDataPool(MediaDataPool<MediaFrame> pool) {
        mPool = pool;
    }
}
