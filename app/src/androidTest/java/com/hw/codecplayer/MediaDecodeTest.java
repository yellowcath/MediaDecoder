package com.hw.codecplayer;

import android.content.Context;
import android.media.Image;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.hw.mediadecoder.MultiMediaDecoder;
import com.hw.mediadecoder.codec.OnFrameDecodeListener;
import com.hw.mediadecoder.demo.util.AssetsUtil;
import com.hw.mediadecoder.domain.MediaData;
import com.hw.mediadecoder.util.CL;
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
public class MediaDecodeTest {
    @Test
    public void testSeek(){
        CL.setLogEnable(true);
        final Context appContext = InstrumentationRegistry.getTargetContext();
        File videoFile1 = new File(appContext.getCacheDir(), "1.mp4");
        File videoFile2 = new File(appContext.getCacheDir(), "2.mp4");
        File videoFile3 = new File(appContext.getCacheDir(), "3.mp4");

        try {
            AssetsUtil.copyAssetsFileTo(appContext, "GOPR1996.MP4", videoFile1.getAbsoluteFile());
            AssetsUtil.copyAssetsFileTo(appContext, "GOPR2002.MP4", videoFile2.getAbsoluteFile());
            AssetsUtil.copyAssetsFileTo(appContext, "GOPR2019.MP4", videoFile3.getAbsoluteFile());

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

        MultiMediaDecoder multiMediaDecoder = new MultiMediaDecoder(dataList);
        multiMediaDecoder.setOnFrameDecodeListener(new OnFrameDecodeListener() {
            @Override
            public void onFrameDecode(Image frameImage, int codecColorFormat, long frameTimeUs, boolean end) {
                CL.i("onFrameDecode,frameTimeUs:" + frameTimeUs + " end:" + end);
                long s = System.currentTimeMillis();
//                ByteBuffer byteBuffer = ByteBuffer.allocate((int) (frameImage.getWidth()*frameImage.getHeight()*1.5f));
                long e = System.currentTimeMillis();
                CL.i("getPlanes:"+(e-s)+"ms");
            }

            @Override
            public void onDecodeError(Throwable t) {
                CL.e("onDecodeError");
                CL.e(t);
            }
        });
        try {
            multiMediaDecoder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        multiMediaDecoder.start();

        SystemClock.sleep(100001);
    }

    @Test
    public void listSyncSamples() throws IOException {
//        Context appContext = InstrumentationRegistry.getTargetContext();
//        File videoFile = new File(appContext.getCacheDir(), "1.mp4");
//        try {
//            AssetsUtil.copyAssetsFileTo(appContext, "1.mp4", videoFile.getAbsoluteFile());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        Map<String, long[]> ss = new LinkedHashMap<String, long[]>();
//        int maxIndex = 0;
//        Movie m = MovieCreator.build(videoFile.getAbsolutePath());
//        for (Track track : m.getTracks()) {
//            if ("vide".equals(track.getHandler())) {
//                ss.put(videoFile.getName() + track.getTrackMetaData().getTrackId(), track.getSyncSamples());
//                maxIndex = Math.max(maxIndex, track.getSyncSamples().length);
//            }
//        }
//        for (String s : ss.keySet()) {
//            System.out.print(String.format("|%10s", s));
//        }
//        System.out.println("|");
//
//        for (int i = 0; i < maxIndex; i++) {
//            for (String s : ss.keySet()) {
//                long[] syncSamples = ss.get(s);
//                try {
//                    System.out.print(String.format("|%10d", syncSamples[i]));
//                } catch (IndexOutOfBoundsException e) {
//                    System.out.print(String.format("|%10s", ""));
//                }
//            }
//            System.out.println("|");
//        }

    }
}
