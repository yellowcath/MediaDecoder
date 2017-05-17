package com.hw.codecplayer.demo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.hw.codecplayer.demo.ui.MySeekbar;
import com.hw.codecplayer.domain.MediaData;
import com.hw.codecplayer.util.CL;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by huangwei on 2017/5/15.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MySeekbar.OnSeekBarChangeListener {

    private static final int DEFAULT_DUTAION = 3000;
    private ImageView mImg1;
    private ImageView mImg2;
    private ImageView mImg3;

    private MySeekbar mSeekbar1;
    private MySeekbar mSeekbar2;
    private MySeekbar mSeekbar3;

    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;

    private Button mBtn1;
    private Button mBtn2;
    private Button mBtn3;

    private Button mPlayBtn;

    private MediaData mMediaData1;
    private MediaData mMediaData2;
    private MediaData mMediaData3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        CL.setLogEnable(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        mImg1 = (ImageView) findViewById(R.id.img1);
        mImg2 = (ImageView) findViewById(R.id.img2);
        mImg3 = (ImageView) findViewById(R.id.img3);

        mSeekbar1 = (MySeekbar) findViewById(R.id.seek1);
        mSeekbar2 = (MySeekbar) findViewById(R.id.seek2);
        mSeekbar3 = (MySeekbar) findViewById(R.id.seek3);

        mTextView1 = (TextView) findViewById(R.id.text1);
        mTextView2 = (TextView) findViewById(R.id.text2);
        mTextView3 = (TextView) findViewById(R.id.text3);

        mBtn1 = (Button) findViewById(R.id.btn1);
        mBtn2 = (Button) findViewById(R.id.btn2);
        mBtn3 = (Button) findViewById(R.id.btn3);

        mPlayBtn = (Button) findViewById(R.id.play);

        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);

        mSeekbar1.setOnSeekBarChangeListener(this);
        mSeekbar2.setOnSeekBarChangeListener(this);
        mSeekbar3.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String videoPath = getPath(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case 1:
                    mMediaData1 = setVideo(videoPath, mImg1, mTextView1, mSeekbar1);
                    break;
                case 2:
                    mMediaData2 = setVideo(videoPath, mImg2, mTextView2, mSeekbar2);
                    break;
                case 3:
                    mMediaData3 = setVideo(videoPath, mImg3, mTextView3, mSeekbar3);
                    break;
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                Toast.makeText(this, "读取失败，请检查文件读写权限", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private MediaData setVideo(String path, ImageView imageView, TextView textView, MySeekbar seekBar) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        Bitmap frame = retriever.getFrameAtTime();
        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

        int duration = Integer.valueOf(durationStr);
        int width = Integer.valueOf(widthStr);
        int height = Integer.valueOf(heightStr);

        imageView.setImageBitmap(frame);
        textView.setText(String.format("%dX%d", width, height));

        if (duration < DEFAULT_DUTAION) {
            throw new RuntimeException("视频太短");
        }
        int startTime = new Random().nextInt((int) (duration - DEFAULT_DUTAION));
        int endTime = startTime + DEFAULT_DUTAION;
        seekBar.setMax(duration);
        seekBar.setProgressLow(startTime);
        seekBar.setProgressHigh(endTime);
        MediaData mediaData = new MediaData(path, startTime, endTime);
        return mediaData;
    }

    private String getPath(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String videoPath = cursor.getString(columnIndex);
            cursor.close();
            return videoPath;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if (v == mBtn1) {
            selectVideo(1);
        } else if (v == mBtn2) {
            selectVideo(2);
        } else if (v == mBtn3) {
            selectVideo(3);
        } else if (v == mPlayBtn) {
            startPlay();
        }
    }

    private void startPlay() {
        if (mMediaData1 == null && mMediaData2 == null && mMediaData3 == null) {
            Toast.makeText(this, "请选择视频", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<MediaData> list = new ArrayList<>();
        if (mMediaData1 != null) {
            list.add(mMediaData1);
        }
        if (mMediaData2 != null) {
            list.add(mMediaData2);
        }
        if (mMediaData3 != null) {
            list.add(mMediaData3);
        }
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putParcelableArrayListExtra("list",list);
        startActivity(intent);
    }

    private void selectVideo(int requestCode) {
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.setType("video/mp4");
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onProgressChanged(MySeekbar seekBar, double progressLow, double progressHigh) {
        CL.i("progressLow：" + progressLow + "  progressHigh:" + progressHigh);
        MediaData mediaData = null;
        if (seekBar == mSeekbar1) {
            mediaData = mMediaData1;
        } else if (seekBar == mSeekbar2) {
            mediaData = mMediaData2;
        } else if (seekBar == mSeekbar3) {
            mediaData = mMediaData3;
        }
        if (mediaData != null) {
            mediaData.startTimeMs = (long) progressLow;
            mediaData.endTimeMs = (long) progressHigh;
        }
    }
}
