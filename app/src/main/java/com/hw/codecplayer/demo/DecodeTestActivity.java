package com.hw.codecplayer.demo;

import android.app.ProgressDialog;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.hw.codecplayer.codec.MediaDecoder;
import com.hw.codecplayer.codec.OnFrameDecodeListener;
import com.hw.codecplayer.domain.MediaData;
import com.hw.codecplayer.util.CL;

import java.util.List;

/**
 * Created by huangwei on 2017/5/17.
 */

public class DecodeTestActivity extends AppCompatActivity {

    private List<MediaData> mDataList;
    private TextView mTitleTxt;
    private TextView mContentTxt;

    private volatile int mFrameCount;
    private volatile long mStartTime;
    private volatile long mCurTime;
    MediaDecoder mediaDecoder;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_decode_test);
        if (getIntent() != null) {
            mDataList = getIntent().getParcelableArrayListExtra("list");
        }
        if (mDataList == null || mDataList.size() == 0) {
            Toast.makeText(this, "无视频数据", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mTitleTxt = (TextView) findViewById(R.id.title);
        mContentTxt = (TextView) findViewById(R.id.content);

        mProgressDialog = ProgressDialog.show(this, "", "加载视频。。。");
        new Thread(new Runnable() {
            @Override
            public void run() {
                initDecoder();
                mProgressDialog.dismiss();
            }
        }).start();
    }

    private void initDecoder() {
        mediaDecoder = new MediaDecoder(mDataList);
        mediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(final Image frameImage, int codecColorFormat, final long frameTimeUs, boolean end) {
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                }
                mCurTime = System.currentTimeMillis();
                mFrameCount++;

                float second = (mCurTime - mStartTime) / 1000f;
                float frameRate = mFrameCount / second;

                String mVideoSize = frameImage.getWidth() + "X" + frameImage.getHeight();
                String content = String.format("size:%s,timestamp:%.2f", mVideoSize, frameImage.getTimestamp() / 1000f / 1000);
                showFrameRate((int) frameRate, content);
                if (mFrameCount > 30) {
                    mStartTime = System.currentTimeMillis();
                    mFrameCount = 0;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaDecoder.start();
    }

    private void showFrameRate(final int frameRate, final String str) {
        mTitleTxt.post(new Runnable() {
            @Override
            public void run() {
                mTitleTxt.setText("解码帧率:" + frameRate);
                mContentTxt.setText(str);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaDecoder != null) {
            mediaDecoder.release();
        }
    }
}
