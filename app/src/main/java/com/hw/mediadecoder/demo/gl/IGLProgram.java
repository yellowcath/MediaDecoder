package com.hw.mediadecoder.demo.gl;

import java.nio.ByteBuffer;

/**
 * Created by huangwei on 2017/5/16.
 */

public interface IGLProgram {
    boolean isProgramBuilt();
    void buildProgram();
    void buildTextures(ByteBuffer y,ByteBuffer u,ByteBuffer v,int w,int h);
    void buildTextures(ByteBuffer y,ByteBuffer uv,int w,int h);
    void drawFrame();
    void createBuffers(float[] vert);
}
