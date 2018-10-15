package com.hw.mediadecoder.util;

import android.media.Image;
import com.hw.mediadecoder.domain.ImageFrame;

/**
 * Created by huangwei on 2018/10/15 0015.
 */
public class ImageFrameAdapter implements MediaDataAdapter<ImageFrame> {

    protected MediaDataPool<ImageFrame> mPool;

    @Override
    public ImageFrame adapte(Image image, int codecColorFormat, final long frameTimeUs) {
        ImageFrame cachedObject = mPool.getCachedObject();
        if (cachedObject != null) {
            return ImageFrame.resetFromImage(image, codecColorFormat, frameTimeUs, cachedObject);
        } else {
            return ImageFrame.createFromImage(image, codecColorFormat, frameTimeUs);
        }
    }

    @Override
    public void setMediaDataPool(MediaDataPool<ImageFrame> pool) {
        mPool = pool;
    }
}
