package com.hw.codecplayer;

import android.content.Context;
import android.media.Image;
import android.media.MediaExtractor;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.hw.codecplayer.codec.MediaLoader;
import com.hw.codecplayer.codec.OnFrameDecodeListener;
import com.hw.codecplayer.demo.util.AssetsUtil;
import com.hw.codecplayer.extractor.MediaData;
import com.hw.codecplayer.extractor.SeekThread;
import com.hw.codecplayer.util.CL;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * Created by huangwei on 2017/5/12.
 */
@RunWith(AndroidJUnit4.class)
public class MediaLoaderTest {
    @Test
    public void testLoader() {
        CL.setLogEnable(true);
        Context appContext = InstrumentationRegistry.getTargetContext();
        File videoFile1 = new File(appContext.getCacheDir(), "1.mp4");

        try {
            AssetsUtil.copyAssetsFileTo(appContext, "1.mp4", videoFile1.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final MediaData data = new MediaData(videoFile1.getAbsolutePath(), 8000, 12000);
        SeekThread seekThread = new SeekThread("seek");
        seekThread.start();
        MediaLoader mCurLoader = new MediaLoader(new MediaExtractor(), data, seekThread, 50);
        mCurLoader.loadAndSeekAsync();
        try {
            mCurLoader.waitSeekFinish(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCurLoader.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(Image frameImage, long frameTimeUs, boolean end) {
                long releativeTimeUs = frameTimeUs - data.startTimeMs * 1000;
                CL.i("onFrameDecode：" + frameTimeUs + " end:" + end + " 相对时间:" + releativeTimeUs / 1000 + "ms");
            }

            @Override
            public void onDecodeError(Throwable t) {

            }
        });

        try {
            mCurLoader.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SystemClock.sleep(100000);
    }
}
