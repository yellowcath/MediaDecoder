package com.hw.codecplayer.domain;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/13.
 */

public class MediaFrame {
    public ByteBuffer buffer1;
    public ByteBuffer buffer2;
    public ByteBuffer buffer3;

    public int pixelStride1;
    public int pixelStride2;
    public int pixelStride3;

    public int rowStride1;
    public int rowStride2;
    public int rowStride3;

    public int width;
    public int height;
    public Rect cropRect;
    public long timestampUs;

    public static MediaFrame resetFromImage(Image image, long timestampUs, MediaFrame mediaFrame) {
        checkFormat(image);
        Image.Plane[] planes = image.getPlanes();
        mediaFrame.buffer1 = copyBuffer(mediaFrame.buffer1, planes[0].getBuffer());
        mediaFrame.buffer2 = copyBuffer(mediaFrame.buffer2, planes[1].getBuffer());
        mediaFrame.buffer3 = copyBuffer(mediaFrame.buffer3, planes[2].getBuffer());

        mediaFrame.pixelStride1 = planes[0].getPixelStride();
        mediaFrame.pixelStride2 = planes[1].getPixelStride();
        mediaFrame.pixelStride3 = planes[2].getPixelStride();

        mediaFrame.rowStride1 = planes[0].getRowStride();
        mediaFrame.rowStride2 = planes[1].getRowStride();
        mediaFrame.rowStride3 = planes[2].getRowStride();

        mediaFrame.width = image.getWidth();
        mediaFrame.height = image.getHeight();
        mediaFrame.cropRect = image.getCropRect();
        mediaFrame.timestampUs = timestampUs;
        return mediaFrame;
    }

    private static ByteBuffer copyBuffer(ByteBuffer dest, ByteBuffer src) {
        int size = src.limit();
        if (dest == null || dest.capacity() < size) {
            dest = ByteBuffer.allocateDirect(size);
        }
        dest.clear();
        dest.put(src);
        dest.flip();
        return dest;
    }

    public static MediaFrame createFromImage(Image image, long timestampUs) {
        checkFormat(image);
        return resetFromImage(image, timestampUs, new MediaFrame());
    }

    private static void checkFormat(Image image) throws UnsupportedOperationException {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new UnsupportedOperationException("only support ImageFormat.YUV_420_888");
        }
    }
}
