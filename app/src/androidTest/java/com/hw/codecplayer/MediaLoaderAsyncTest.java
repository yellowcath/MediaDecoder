package com.hw.codecplayer;

import android.content.Context;
import android.media.Image;
import android.media.MediaExtractor;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.hw.mediadecoder.codec.MediaDecoder;
import com.hw.mediadecoder.codec.OnFrameDecodeListener;
import com.hw.mediadecoder.demo.util.AssetsUtil;
import com.hw.mediadecoder.demo.util.DecodeTrace;
import com.hw.mediadecoder.domain.MediaData;
import com.hw.mediadecoder.util.CL;
import com.hw.mediadecoder.util.RunnableThread;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * Created by huangwei on 2017/5/12.
 */
@RunWith(AndroidJUnit4.class)
public class MediaLoaderAsyncTest {
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
        RunnableThread seekThread = new RunnableThread("seek");
        seekThread.start();
        MediaDecoder mCurLoader = new MediaDecoder(new MediaExtractor(), data, seekThread, 50);
        mCurLoader.loadAndSeekAsync();
        try {
            mCurLoader.waitSeekFinish(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCurLoader.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(Image frameImage, int codecColorFormat, long frameTimeUs, boolean end) {
                DecodeTrace.onFrameDecode();
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
        SystemClock.sleep(1000000);
    }
}
