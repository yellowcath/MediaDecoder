package com.hw.codecplayer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testBuffer() throws Exception {
        byte[] bytes1 = new byte[10];
        Arrays.fill(bytes1, (byte) 1);
        byte[] bytes2 = new byte[10];
        Arrays.fill(bytes2, (byte) 2);
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(10);
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(10);
        buffer1.put(bytes1);
        buffer2.put(bytes2);

        buffer1.flip();
        buffer2.flip();
        ByteBuffer buffer = ByteBuffer.allocateDirect(30);
        buffer.put(buffer1);
        buffer.put(buffer2);
        byte[] bytes = new byte[20];
        buffer.flip();
        buffer.get(bytes);
        assertEquals(4, 2 + 2);
    }
}