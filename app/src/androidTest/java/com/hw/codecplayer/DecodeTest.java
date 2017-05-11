package com.hw.codecplayer;

import android.content.Context;
import android.media.Image;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.hw.codecplayer.codec.MediaDecoder;
import com.hw.codecplayer.codec.OnFrameDecodeListener;
import com.hw.codecplayer.demo.util.AssetsUtil;
import com.hw.codecplayer.extractor.MediaData;
import com.hw.codecplayer.extractor.MediaSource;
import com.hw.codecplayer.util.CL;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2017/5/11.
 */
@RunWith(AndroidJUnit4.class)
public class DecodeTest {
    @Test
    public void testDecode(){
        CL.setLogEnable(true);
        Context appContext = InstrumentationRegistry.getTargetContext();
        File videoFile1 = new File(appContext.getCacheDir(), "1.mp4");
        File videoFile2 = new File(appContext.getCacheDir(), "2.mp4");
        File videoFile3 = new File(appContext.getCacheDir(), "3.mp4");

        try {
            AssetsUtil.copyAssetsFileTo(appContext, "1.mp4", videoFile1.getAbsoluteFile());
            AssetsUtil.copyAssetsFileTo(appContext, "2.mp4", videoFile2.getAbsoluteFile());
            AssetsUtil.copyAssetsFileTo(appContext, "3.mp4", videoFile3.getAbsoluteFile());

        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaData mediaData1 = new MediaData(videoFile1.getAbsolutePath(), 8000, 12000);
        MediaData mediaData2 = new MediaData(videoFile2.getAbsolutePath(), 8000, 12000);
        MediaData mediaData3 = new MediaData(videoFile3.getAbsolutePath(), 8000, 12000);

        List<MediaData> dataList = new ArrayList<>();
        dataList.add(mediaData1);
        dataList.add(mediaData2);
        dataList.add(mediaData3);

        MediaSource mediaSource = new MediaSource();
        try {
            mediaSource.setDataSource(dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaDecoder mediaDecoder = new MediaDecoder();
        mediaDecoder.setMediaSource(mediaSource);
        mediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(Image frameImage, long frameTimeUs) {
                CL.i("onFrameDecode:"+frameTimeUs/1000f/1000f+"s");
            }
        });
        try {
            mediaDecoder.prepare();
            mediaDecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
