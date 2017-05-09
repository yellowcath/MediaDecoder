package com.hw.codecplayer;

import android.content.Context;
import android.media.MediaFormat;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.hw.codecplayer.demo.util.AssetsUtil;
import com.hw.codecplayer.demo.util.CL;
import com.hw.codecplayer.extractor.MediaData;
import com.hw.codecplayer.extractor.MediaSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2017/5/9.
 */
@RunWith(AndroidJUnit4.class)
public class MediaSourceTest {
    @Test
    public void testSeek(){
        CL.setLogEnable(true);
        Context appContext = InstrumentationRegistry.getTargetContext();
        File videoFile = new File(appContext.getCacheDir(),"1.mp4");
        try {
            AssetsUtil.copyAssetsFileTo(appContext,"1.mp4",videoFile.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaData mediaData = new MediaData(videoFile.getAbsolutePath(),8000,12000);
        List<MediaData> dataList = new ArrayList<>();
        dataList.add(mediaData);

        MediaSource mediaSource = new MediaSource();
        try {
            mediaSource.setDataSource(dataList);
            MediaFormat currentMediaFormat = mediaSource.getCurrentMediaFormat();
            int integer = currentMediaFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
