package com.hw.codecplayer.demo;

import android.content.Context;
import android.media.Image;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.hw.codecplayer.codec.MediaDecoder;
import com.hw.codecplayer.codec.OnFrameDecodeListener;
import com.hw.codecplayer.demo.gl.GLFrameRenderer;
import com.hw.codecplayer.demo.util.AssetsUtil;
import com.hw.codecplayer.domain.MediaData;
import com.hw.codecplayer.domain.MediaFrame;
import com.hw.codecplayer.util.CL;
import com.hw.codecplayer.util.MediaFramePool;
import com.hw.codecplayer.util.RunnableThread;

import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends AppCompatActivity {

    private MediaDecoder mMediaDecoder;
    private GLSurfaceView mPreviewView;
    private TextView mTextView;

    private long mStartTime;
    private RunnableThread mSeekThread;
    private MediaFramePool mFramePool = new MediaFramePool(10, 10);
    private volatile int mFrameDrawCount;
    private volatile long mStartDrawTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CL.setLogEnable(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        mTextView = (TextView) findViewById(R.id.textview);
        mPreviewView = (GLSurfaceView) findViewById(R.id.previewview);
        mPreviewView.setEGLContextClientVersion(2);

        mSeekThread = new RunnableThread("123");
        mSeekThread.start();
        initDecoder();
    }

    private void showFrameRate() {
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                float second = (System.currentTimeMillis() - mStartDrawTime) / 1000f;
                float frameRate = mFrameDrawCount / second;
                mTextView.setText("fps:" + (int)frameRate);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFrameDrawCount = 0;
        mStartDrawTime = 0;
    }

    private void initDecoder() {
        CL.setLogEnable(true);
        Context appContext = getApplicationContext();
        File videoFile1 = new File(appContext.getCacheDir(), "1.mp4");
        File videoFile2 = new File(appContext.getCacheDir(), "2.mp4");
        File videoFile3 = new File(appContext.getCacheDir(), "3.mp4");

        try {
            AssetsUtil.copyAssetsFileTo(appContext, "1.mp4", videoFile1.getAbsoluteFile());
//            AssetsUtil.copyAssetsFileTo(appContext, "GOPR2002.MP4", videoFile2.getAbsoluteFile());
//            AssetsUtil.copyAssetsFileTo(appContext, "GOPR2019.MP4", videoFile3.getAbsoluteFile());

        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaData mediaData1 = new MediaData(videoFile1.getAbsolutePath(), 8000, 12000);
//        MediaData mediaData2 = new MediaData(videoFile2.getAbsolutePath(), 8000, 12000);
//        MediaData mediaData3 = new MediaData(videoFile3.getAbsolutePath(), 20000, 24000);

        List<MediaData> dataList = new ArrayList<>();
        dataList.add(mediaData1);
//        dataList.add(mediaData2);
//        dataList.add(mediaData3);
        MediaDecoder mediaDecoder = new MediaDecoder(dataList);
        mediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(final Image frameImage, final long frameTimeUs, boolean end) {
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                }
                if (end) {
                    long time = System.currentTimeMillis() - mStartTime;
                    CL.i("总计耗时:" + time + "ms");
                }
                if(CL.isLogEnable()) {
//                    String imageStr = String.format("%dX%d,cropRect:%s,format:%d", frameImage.getWidth(), frameImage.getHeight(), frameImage.getCropRect().toShortString(), frameImage.getFormat());
//                    String uvplaneStr = String.format("pixelStride:%d,rowStride:%d", frameImage.getPlanes()[1].getPixelStride(), frameImage.getPlanes()[1].getRowStride());
                    CL.i("onFrameDecode,frameTimeUs:" + frameTimeUs + " end:" + end);
//                    CL.i(imageStr+" \nuv:"+uvplaneStr);
                }
                if (!end) {
                    offerImage(frameImage, frameTimeUs);
                }
            }

            @Override
            public void onDecodeError(Throwable t) {
                CL.e("onDecodeError");
                CL.e(t);
            }
        });
        try {
            mediaDecoder.setLoop(true);
            mediaDecoder.prepare();
            MediaFormat currentMediaFormat = mediaDecoder.getCurrentMediaFormat();
            int w = currentMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            int h = currentMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);

            final GLFrameRenderer renderer = new GLFrameRenderer(mPreviewView, getResources().getDisplayMetrics(), mFramePool,
                    currentMediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT)) {

                @Override
                public void onDrawFrame(GL10 gl) {
                    super.onDrawFrame(gl);
                    mFrameDrawCount++;
                    if (mStartDrawTime == 0) {
                        mStartDrawTime = System.currentTimeMillis();
                    }
                    showFrameRate();
                }
            };
            mPreviewView.setRenderer(renderer);
            renderer.update(w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaDecoder.start();
    }

    private void offerImage(Image image, long timeUs) {
        MediaFrame mediaFrame = mFramePool.getCachedObject();
        mediaFrame = mediaFrame == null ? MediaFrame.createFromImage(image, timeUs) : MediaFrame.resetFromImage(image, timeUs, mediaFrame);
        mFramePool.offer(mediaFrame);
    }
//    private void offerImage(Image image, long timeUs) {
//        MyImage myImage = new MyImage();
//        myImage.timeUs = timeUs;
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer bufferY = planes[0].getBuffer();
//        ByteBuffer bufferUV = planes[1].getBuffer();
//
//        myImage.y = new byte[planes[0].getBuffer().remaining()];
//        bufferY.get(myImage.y);
//        int uSize = image.getWidth() * image.getHeight() / 4;
//        myImage.u = new byte[uSize];
//        myImage.v = new byte[uSize];
//        int uIndex = 0, vIndex = 0;
//        int uvSize = bufferUV.remaining();
//        for (int i = 0; i < uvSize; i++) {
//            if (i % 2 == 0) {
//                myImage.u[uIndex++] = bufferUV.get(i);
//            } else {
//                myImage.v[vIndex++] = bufferUV.get(i);
//            }
//        }
//        try {
//            FrameQueue.offer(myImage);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
