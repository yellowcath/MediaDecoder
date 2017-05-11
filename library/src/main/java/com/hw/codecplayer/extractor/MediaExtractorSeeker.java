package com.hw.codecplayer.extractor;

import android.media.MediaExtractor;
import com.hw.codecplayer.util.CL;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.hw.codecplayer.extractor.MediaSource.getVideoTrackIndex;

/**
 * Created by huangwei on 2017/5/11.
 */

public class MediaExtractorSeeker {
    private static final int TIME_OUT_MS = 10000;
    private CountDownLatch mCountDownLatch;
    private SeekThread mHandlerThread;
    private MediaExtractor mMediaExtractor;
    private IOException mThrowable;
    private long mSeekAccuracyMs;

    public MediaExtractorSeeker(MediaExtractor mediaExtractor, SeekThread seekThread, long seekAccuracyMs) {
        mHandlerThread = seekThread;
        mMediaExtractor = mediaExtractor;
        mSeekAccuracyMs = seekAccuracyMs;
        mCountDownLatch = new CountDownLatch(1);
    }

    public static void seekSync(MediaExtractor extractor, long timeUs, long seekAccuracyMs) {
        long seekToTimeMs = timeUs / 1000;
        long s = System.currentTimeMillis();
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long seekSampleTimeMs = extractor.getSampleTime() / 1000;
        CL.i("seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
        int index = 0;
        while (seekToTimeMs - seekSampleTimeMs > seekAccuracyMs) {
            index++;
            extractor.advance();
            seekSampleTimeMs = extractor.getSampleTime() / 1000;
            CL.i("循环seek,seekToMs:" + seekToTimeMs + " seekSampleTimeMs:" + seekSampleTimeMs);
        }
        long e = System.currentTimeMillis();
        CL.i(String.format("seek成功，循环%d次，耗时:%dms,seekTo:%d,seekSampleTimeMs:%d", index, e - s, seekToTimeMs, seekSampleTimeMs));
    }

    public boolean waitSeekFinish() throws TimeoutException, IOException {
        return waitSeekFinish(TIME_OUT_MS);
    }

    public boolean waitSeekFinish(int timeout) throws TimeoutException, IOException {
        try {
            boolean wait = mCountDownLatch.await(timeout, TimeUnit.MILLISECONDS);
            if (!wait) {
                throw new TimeoutException("wait time out");
            } else if (mThrowable != null) {
                throw mThrowable;
            }
            return wait;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadAndSeekAsync(final MediaExtractor extractor, final MediaData mediaData) {
        mThrowable = null;
        mHandlerThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    extractor.setDataSource(mediaData.mediaPath);
                    int videoTrackIndex = getVideoTrackIndex(extractor);
                    extractor.selectTrack(videoTrackIndex);
                    if (mediaData.shouldCut) {
                        seekSync(extractor, mediaData.startTimeMs * 1000, mSeekAccuracyMs);
                    }
                } catch (IOException e) {
                    mThrowable = e;
                }
                mCountDownLatch.countDown();
            }
        });
    }
}
