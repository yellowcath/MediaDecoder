package com.hw.mediadecoder.domain;

import android.graphics.ImageFormat;
import android.media.Image;
import com.hw.mediadecoder.util.NativeUtil;

import java.nio.ByteBuffer;


/**
 * Created by huangwei on 2017/5/13.
 * 提供整体的YUV
 */
public class VideoFrame {
    //YuvType.YUV
    protected ByteBuffer bufferYUV;
    //YuvType.YUV420P
    protected ByteBuffer bufferY;
    protected ByteBuffer bufferU;
    protected ByteBuffer bufferV;
    //YuvType.YUV420SP
    protected ByteBuffer bufferUV;

    public int width;
    public int height;
    public long timestampUs;
    /**
     * 从MediaCodec获取的颜色值，注意不是从Image里面获取的
     * {@link android.media.MediaCodecInfo.CodecCapabilities}
     */
    public int codecColorFormat;

    private YuvType mOutputYuvType;

    public enum YuvType {
        YUV420P,
        YUV420SP,
        YUV
    }

    public VideoFrame(YuvType outputYuvType) {
        mOutputYuvType = outputYuvType;
    }

    public YuvType getOutputYuvType() {
        return mOutputYuvType;
    }

    private void initBuffer(int width, int height) {
        switch (mOutputYuvType) {
            case YUV:
                if (bufferYUV == null) {
                    bufferYUV = ByteBuffer.allocateDirect(width * height * 3 / 2);
                }
                break;
            case YUV420P:
                if (bufferY == null) {
                    bufferY = ByteBuffer.allocateDirect(width * height);
                    bufferU = ByteBuffer.allocateDirect(width * height / 4);
                    bufferV = ByteBuffer.allocateDirect(width * height / 4);
                }
                break;
            case YUV420SP:
                if (bufferY == null) {
                    bufferY = ByteBuffer.allocateDirect(width * height);
                    bufferUV = ByteBuffer.allocateDirect(width * height / 2);
                }
                break;
        }
    }

    public static VideoFrame resetFromImage(Image image, int codecColorFormat, long timestampUs, VideoFrame mediaFrame) {
        checkFormat(image);
        Image.Plane[] planes = image.getPlanes();
        mediaFrame.width = image.getWidth();
        mediaFrame.height = image.getHeight();
        mediaFrame.timestampUs = timestampUs;
        mediaFrame.codecColorFormat = codecColorFormat;

        mediaFrame.initBuffer(image.getWidth(),image.getHeight());

        switch (mediaFrame.getOutputYuvType()) {
            case YUV:
                NativeUtil.planesToYUV(planes[0].getBuffer(), planes[1].getBuffer(), planes[2].getBuffer(),
                        planes[0].getBuffer().capacity(), planes[1].getBuffer().capacity(), planes[2].getBuffer().capacity(),
                        planes[0].getPixelStride(), planes[1].getPixelStride(), planes[2].getPixelStride(),
                        planes[0].getRowStride(), planes[1].getRowStride(), planes[2].getRowStride(),
                        image.getWidth(), image.getHeight(),
                        mediaFrame.bufferYUV
                );
                break;
            case YUV420P:
                NativeUtil.planesToYUV420p(planes[0].getBuffer(), planes[1].getBuffer(), planes[2].getBuffer(),
                        planes[0].getBuffer().capacity(), planes[1].getBuffer().capacity(), planes[2].getBuffer().capacity(),
                        planes[0].getPixelStride(), planes[1].getPixelStride(), planes[2].getPixelStride(),
                        planes[0].getRowStride(), planes[1].getRowStride(), planes[2].getRowStride(),
                        image.getWidth(), image.getHeight(),
                        mediaFrame.bufferY,mediaFrame.bufferU,mediaFrame.bufferV
                );
                break;
            case YUV420SP:
                NativeUtil.planesToYUV420sp(planes[0].getBuffer(), planes[1].getBuffer(), planes[2].getBuffer(),
                        planes[0].getBuffer().capacity(), planes[1].getBuffer().capacity(), planes[2].getBuffer().capacity(),
                        planes[0].getPixelStride(), planes[1].getPixelStride(), planes[2].getPixelStride(),
                        planes[0].getRowStride(), planes[1].getRowStride(), planes[2].getRowStride(),
                        image.getWidth(), image.getHeight(),
                        mediaFrame.bufferY,mediaFrame.bufferUV
                );
                break;
        }

        return mediaFrame;
    }

    public static VideoFrame createFromImage(Image image, int codecColorFormat, long timestampUs, YuvType yuvType) {
        checkFormat(image);
        return resetFromImage(image, codecColorFormat, timestampUs, new VideoFrame(yuvType));
    }

    public ByteBuffer getBufferYUV420P() {
        return bufferYUV;
    }

    private static void checkFormat(Image image) throws UnsupportedOperationException {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new UnsupportedOperationException("only support ImageFormat.YUV_420_888");
        }
    }

    public ByteBuffer getBufferYUV() {
        if(mOutputYuvType!=YuvType.YUV){
            throw new RuntimeException("outputYuvType isn't YuvType.YUV!");
        }
        return bufferYUV;
    }

    public ByteBuffer getBufferY() {
        if(mOutputYuvType==YuvType.YUV){
            throw new RuntimeException("outputYuvType isn't YuvType.YUV420p or YuvType.YUV420sp");
        }
        return bufferY;
    }

    public ByteBuffer getBufferU() {
        if(mOutputYuvType!=YuvType.YUV420P){
            throw new RuntimeException("outputYuvType isn't YuvType.YUV420p");
        }
        return bufferU;
    }

    public ByteBuffer getBufferV() {
        if(mOutputYuvType!=YuvType.YUV420P){
            throw new RuntimeException("outputYuvType isn't YuvType.YUV420p");
        }
        return bufferV;
    }

    public ByteBuffer getBufferUV() {
        if(mOutputYuvType!=YuvType.YUV420SP){
            throw new RuntimeException("outputYuvType isn't YuvType.YUV420sp");
        }
        return bufferUV;
    }
}
