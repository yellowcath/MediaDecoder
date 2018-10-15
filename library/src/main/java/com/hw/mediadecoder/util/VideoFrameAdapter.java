package com.hw.mediadecoder.util;

import android.media.Image;
import com.hw.mediadecoder.domain.VideoFrame;

/**
 * Created by huangwei on 2018/10/15 0015.
 */
public class VideoFrameAdapter implements MediaDataAdapter<VideoFrame> {

    protected MediaDataPool<VideoFrame> mPool;
    private VideoFrame.YuvType mOutYuvType;

    public VideoFrameAdapter(VideoFrame.YuvType outYuvType) {
        mOutYuvType = outYuvType;
    }

    @Override
    public VideoFrame adapte(Image image, int codecColorFormat, final long frameTimeUs) {
        VideoFrame cachedObject = mPool.getCachedObject();
        if (cachedObject != null) {
            return VideoFrame.resetFromImage(image, codecColorFormat, frameTimeUs, cachedObject);
        } else {
            return VideoFrame.createFromImage(image, codecColorFormat, frameTimeUs,mOutYuvType);
        }
    }

    @Override
    public void setMediaDataPool(MediaDataPool<VideoFrame> pool) {
        mPool = pool;
    }
}
