package com.hw.mediadecoder.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.Image;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.hw.mediadecoder.MultiMediaDecoder;
import com.hw.mediadecoder.codec.OnFrameDecodeListener;
import com.hw.mediadecoder.demo.gl.GLFrameRenderer;
import com.hw.mediadecoder.domain.MediaData;
import com.hw.mediadecoder.domain.VideoFrame;
import com.hw.mediadecoder.util.CL;
import com.hw.mediadecoder.util.MediaDataPool;
import com.hw.mediadecoder.util.RunnableThread;
import com.hw.mediadecoder.util.VideoFrameAdapter;

import javax.microedition.khronos.opengles.GL10;
import java.util.List;

public class PlayActivity extends AppCompatActivity {

    private GLSurfaceView mPreviewView;
    private TextView mTextView;

    private long mStartTime;
    private RunnableThread mSeekThread;
    private MediaDataPool<VideoFrame> mFramePool = new MediaDataPool<VideoFrame>(10, 10, new VideoFrameAdapter(VideoFrame.YuvType.YUV420SP));
    private volatile int mFrameDrawCount;
    private volatile long mStartDrawTime;
    private GLFrameRenderer mFrameRenderer;
    private List<MediaData> mDataList;
    private int mCodecColorFormat;
    private String mVideoSize;
    private int mFrameRate;
    private ProgressDialog mProgressDialog;
    private MultiMediaDecoder mMultiMediaDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        if (getIntent() != null && getIntent().hasExtra("list")) {
            mDataList = getIntent().getParcelableArrayListExtra("list");
        }
//        else {
//            CL.setLogEnable(true);
//            mDataList = new ArrayList<>();
//            mDataList.add(new MediaData("/sdcard/g.mp4"));
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mMultiMediaDecoder.release();
//                }
//            }, 5000);
//        }
        if (mDataList == null || mDataList.size() == 0) {
            Toast.makeText(this, "无视频数据", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mTextView = (TextView) findViewById(R.id.textview);
        mPreviewView = (GLSurfaceView) findViewById(R.id.previewview);
        mPreviewView.setEGLContextClientVersion(2);

        mSeekThread = new RunnableThread("123");
        mSeekThread.start();

        mFrameRenderer = new GLFrameRenderer(mPreviewView, getResources().getDisplayMetrics(), mFramePool) {

            @Override
            public void onDrawFrame(GL10 gl) {
                super.onDrawFrame(gl);
                mFrameDrawCount++;
                if (mStartDrawTime == 0) {
                    mStartDrawTime = System.currentTimeMillis();
                }
                if (mFrameDrawCount > 30) {
                    mFrameDrawCount = 0;
                    mStartDrawTime = System.currentTimeMillis();
                } else {
                    showFrameRate();
                }
            }
        };
        mPreviewView.setRenderer(mFrameRenderer);
        mProgressDialog = ProgressDialog.show(this, "", "加载视频。。。");
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDecoder();
                mProgressDialog.dismiss();
            }
        }).start();
    }

    private void showFrameRate() {
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                float second = (System.currentTimeMillis() - mStartDrawTime) / 1000f;
                float frameRate = mFrameDrawCount / second;
                if (mVideoSize == null) {
                    mVideoSize = "";
                }
                mTextView.setText("fps:" + (int) frameRate
                        + "\n视频帧率:" + (mFrameRate == -1 ? "未知" : mFrameRate)
                        + " \ncolorFormat:" + mCodecColorFormat
                        + "\n" + mVideoSize);
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
        Context appContext = getApplicationContext();
//        File videoFile1 = new File(appContext.getCacheDir(), "1.mp4");
//        File videoFile2 = new File(appContext.getCacheDir(), "2.mp4");
//        File videoFile3 = new File(appContext.getCacheDir(), "3.mp4");
//
//        try {
//            AssetsUtil.copyAssetsFileTo(appContext, "GOPR1996.MP4", videoFile1.getAbsoluteFile());
////            AssetsUtil.copyAssetsFileTo(appContext, "GOPR2002.MP4", videoFile2.getAbsoluteFile());
//            AssetsUtil.copyAssetsFileTo(appContext, "GOPR2019.MP4", videoFile2.getAbsoluteFile());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        MediaData mediaData1 = new MediaData(videoFile1.getAbsolutePath(), 8000, 12000);
//        MediaData mediaData2 = new MediaData(videoFile2.getAbsolutePath(), 8000, 12000);
////        MediaData mediaData3 = new MediaData(videoFile3.getAbsolutePath(), 20000, 24000);
//
//        List<MediaData> dataList = new ArrayList<>();
//        dataList.add(mediaData1);
//        dataList.add(mediaData2);
////        dataList.add(mediaData3);
        mMultiMediaDecoder = new MultiMediaDecoder(mDataList);
        mMultiMediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(final Image frameImage, int codecColorFormat, final long frameTimeUs, boolean end) {
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                }
                if (end) {
                    long time = System.currentTimeMillis() - mStartTime;
                    CL.i("总计耗时:" + time + "ms");
                }
                mVideoSize = frameImage.getWidth() + "X" + frameImage.getHeight();
                mCodecColorFormat = codecColorFormat;
                MediaFormat currentMediaFormat = mMultiMediaDecoder.getExtractorMediaFormat();
                mFrameRate = currentMediaFormat.containsKey(MediaFormat.KEY_FRAME_RATE) ?
                        currentMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE) : -1;
                if (CL.isLogEnable()) {
//                    String imageStr = String.format("%dX%d,cropRect:%s,format:%d", frameImage.getWidth(), frameImage.getHeight(), frameImage.getCropRect().toShortString(), frameImage.getFormat());
//                    String uvplaneStr = String.format("pixelStride:%d,rowStride:%d", frameImage.getPlanes()[1].getPixelStride(), frameImage.getPlanes()[1].getRowStride());
                    CL.i("onFrameDecode,frameTimeUs:" + frameTimeUs + " end:" + end);
//                    CL.i(imageStr+" \nuv:"+uvplaneStr);
                }
                if (!end) {
                    offerImage(frameImage, codecColorFormat, frameTimeUs);
                }

            }

            @Override
            public void onDecodeError(Throwable t) {
                CL.e("onDecodeError");
                CL.e(t);
            }
        });
        try {
            mMultiMediaDecoder.setLoop(true);
            mMultiMediaDecoder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMultiMediaDecoder.start();
    }

    private void offerImage(Image image, int codecColorFormat, long timeUs) {
        mFramePool.offer(image, codecColorFormat, timeUs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMultiMediaDecoder != null) {
            mMultiMediaDecoder.release();
        }
        if (mFrameRenderer != null) {
            mFrameRenderer.destroy();
        }
    }
}
