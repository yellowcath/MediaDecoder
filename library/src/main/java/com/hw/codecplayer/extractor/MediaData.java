package com.hw.codecplayer.extractor;

/**
 * Created by huangwei on 2017/5/9.
 */

public class MediaData {
    public String mediaPath;
    public long startTimeMs;
    public long endTimeMs;
    /**
     * 是否按指定时间段截取数据
     */
    public boolean shouldCut;

    public MediaData(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public MediaData(String mediaPath, long startTimeMs, long endTimeMs) {
        this.mediaPath = mediaPath;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        shouldCut = true;
    }
}
