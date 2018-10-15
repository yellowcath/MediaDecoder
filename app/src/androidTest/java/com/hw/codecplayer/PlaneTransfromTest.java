package com.hw.codecplayer;

import android.support.test.runner.AndroidJUnit4;
import com.hw.mediadecoder.util.CL;
import com.hw.mediadecoder.util.NativeUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by huangwei on 2017/5/15.
 */
@RunWith(AndroidJUnit4.class)
public class PlaneTransfromTest {
    @Test
    public void test() {
        CL.setLogEnable(true);
        int width = 4;
        int height = 6;
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(24);
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(12);
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(12);

        byte[] y = new byte[24];
        byte[] u = new byte[]{
                2, 0, 2, 0,
                2, 0, 2, 0,
                2, 0, 2, 0};
        byte[] v = new byte[]{
                3, 0, 4, 0,
                5, 0, 6, 0,
                7, 0, 8, 0};

        Arrays.fill(y, (byte) 1);

        buffer1.put(y);
        buffer2.put(u);
        buffer3.put(v);

        int capacity1 = buffer1.capacity();
        int capacity2 = buffer2.capacity();
        int capacity3 = buffer3.capacity();

        int pixelStride1 = 1;
        int rowStride1 = 4;
        int pixelStride2 = 2;
        int rowStride2 = 4;
        int pixelStride3 = 2;
        int rowStride3 = 4;
        {
            ByteBuffer bufferYUV = ByteBuffer.allocateDirect(width * height * 3 / 2);

            NativeUtil.planesToYUV(buffer1, buffer2, buffer3,
                    capacity1, capacity2, capacity3,
                    pixelStride1, pixelStride2, pixelStride3,
                    rowStride1, rowStride2, rowStride3,
                    width, height, bufferYUV);
            byte[] result = new byte[bufferYUV.capacity()];
            bufferYUV.get(result, 0, bufferYUV.capacity());

            CL.i("result is:\n" + Arrays.toString(result));
        }

//        {
//            ByteBuffer bufferY = ByteBuffer.allocateDirect(width * height);
//            ByteBuffer bufferU = ByteBuffer.allocateDirect(width * height / 4);
//            ByteBuffer bufferV = ByteBuffer.allocateDirect(width * height / 4);
//
//            NativeUtil.planesToYUV420p(buffer1, buffer2, buffer3,
//                    capacity1, capacity2, capacity3,
//                    pixelStride1, pixelStride2, pixelStride3,
//                    rowStride1, rowStride2, rowStride3,
//                    width, height, bufferY, bufferU, bufferV);
//            byte[] result = new byte[bufferY.capacity() + bufferU.capacity() + bufferV.capacity()];
//            bufferY.get(result, 0, bufferY.capacity());
//            bufferU.get(result, bufferY.capacity(), bufferU.capacity());
//            bufferV.get(result, bufferY.capacity() + bufferU.capacity(), bufferV.capacity());
//
//            CL.i("result is:\n" + Arrays.toString(result));
//        }
    }
}
