package com.hw.codecplayer.domain;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import com.hw.codecplayer.util.CL;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/13.
 */

public class MediaFrame {
    public ByteBuffer YUVBuffer;
    public int width;
    public int height;
    public Rect cropRect;
    public long timestampUs;

    public static MediaFrame resetFromImage(Image image, long timestampUs, MediaFrame mediaFrame) {
        checkFormat(image);
        int capacity = mediaFrame.YUVBuffer.capacity();
        Image.Plane[] planes = image.getPlanes();
        int ySize = planes[0].getBuffer().limit();
        int uvSize = +planes[1].getBuffer().limit();
        int size = ySize+uvSize;
        if(capacity<size){
            CL.e("原buffer空间不足:"+capacity+" 重新申请:"+size);
            mediaFrame.YUVBuffer = ByteBuffer.allocateDirect(size);
        }else{
            mediaFrame.YUVBuffer.clear();
        }
        mediaFrame.YUVBuffer.put(planes[0].getBuffer());
        mediaFrame.YUVBuffer.put(planes[1].getBuffer());
        mediaFrame.YUVBuffer.flip();

        mediaFrame.width = image.getWidth();
        mediaFrame.height = image.getHeight();
        mediaFrame.cropRect = image.getCropRect();
        mediaFrame.timestampUs = timestampUs;
        return mediaFrame;
    }

    public static MediaFrame createFromImage(Image image, long timestampUs) {
        checkFormat(image);
        MediaFrame mediaFrame = new MediaFrame();
        Image.Plane[] planes = image.getPlanes();
        int ySize = planes[0].getBuffer().limit();
        int uvSize = +planes[1].getBuffer().limit();
        int size = ySize+uvSize;
        mediaFrame.YUVBuffer = ByteBuffer.allocateDirect(size);
        mediaFrame.YUVBuffer.put(planes[0].getBuffer());
        mediaFrame.YUVBuffer.put(planes[1].getBuffer());
        mediaFrame.YUVBuffer.flip();

        mediaFrame.width = image.getWidth();
        mediaFrame.height = image.getHeight();
        mediaFrame.cropRect = image.getCropRect();
        mediaFrame.timestampUs = timestampUs;
        return mediaFrame;
    }

    private static void checkFormat(Image image) throws UnsupportedOperationException{
        if(image.getFormat()!= ImageFormat.YUV_420_888){
            throw new UnsupportedOperationException("only support ImageFormat.YUV_420_888");
        }
    }
}
