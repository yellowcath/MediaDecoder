package com.hw.codecplayer.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.hw.codecplayer.demo.ui.MySeekbar;

/**
 * Created by huangwei on 2017/5/15.
 */

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mImg1 = (ImageView) findViewById(R.id.img1);
        mImg2 = (ImageView) findViewById(R.id.img2);
        mImg3 = (ImageView) findViewById(R.id.img3);

    }
}
