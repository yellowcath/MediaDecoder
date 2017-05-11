package com.hw.codecplayer.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.hw.codecplayer.util.CL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CL.setLogEnable(true);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
    }
}
