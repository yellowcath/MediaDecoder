package com.hw.codecplayer.util;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

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

    public static boolean useUVBuffer(int colorFormat) {
        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
                || colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar
                || colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar+4) {
            return true;
        }
        return false;
    }

    public static MediaCodec configDecoder(MediaFormat format) {
        if (format == null) {
            return null;
        }
        MediaCodec codec;
        String mime = format.getString(MediaFormat.KEY_MIME);
        MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] infos = list.getCodecInfos();

        for (MediaCodecInfo info : infos) {

            MediaCodecInfo.CodecCapabilities capabilities;
            boolean formatSupported;

            // does codec support this mime type
            try {
                capabilities = info.getCapabilitiesForType(mime);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            // does codec support his video format
            try {
                formatSupported = capabilities.isFormatSupported(format);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            // can we configure it successfully
            if (formatSupported) {
                // try decoder
                try {
                    codec = MediaCodec.createByCodecName(info.getName());
                } catch (IOException e) {
                    continue;
                }
                try {
                    codec.configure(format, null, null, 0);
                } catch (IllegalArgumentException ignored) {
                    // configure() failed
                    codec.release();
                    continue;
                } catch (IllegalStateException ignored) {
                    // configure() failed
                    codec.release();
                    continue;
                }
                // configure() successful
                return codec;
            }
        } // end of for loop

        // no decoder found
        return null;
    }

    public static boolean tryCheckEmpty(ByteBuffer byteBuffer){
        int checkCount = 50;
        for(int i=0;i<checkCount;i++){
            if(byteBuffer.get(i)!=0){
                return false;
            }
        }

        int mid = byteBuffer.capacity()/2;
        for(int i=mid;i<mid + checkCount;i++){
            if(byteBuffer.get(i)!=0){
                return false;
            }
        }
        return true;
    }
}
