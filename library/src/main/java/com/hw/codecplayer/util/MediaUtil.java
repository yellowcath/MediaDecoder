package com.hw.codecplayer.util;

import android.media.MediaExtractor;
import android.media.MediaFormat;

/**
 * Created by huangwei on 2017/5/12.
 */

public class MediaUtil {
    public static int getVideoTrackIndex(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            //获取码流的详细格式/配置信息
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }
}
