package com.hw.mediadecoder.demo.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import com.hw.mediadecoder.domain.VideoFrame;
import com.hw.mediadecoder.util.CL;
import com.hw.mediadecoder.util.MediaDataPool;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLFrameRenderer implements Renderer {

    private GLSurfaceView mTargetSurfaceView;
    private IGLProgram mProgram;
    private int mScreenWidth, mScreenHeight;
    private int mVideoWidth, mVideoHeight;
    private MediaDataPool<VideoFrame> mMediaDataPool;

    public GLFrameRenderer(GLSurfaceView surface, DisplayMetrics dm, MediaDataPool<VideoFrame> pool) {
        mTargetSurfaceView = surface;
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mMediaDataPool = pool;
    }

    private void checkInitProgram() {
        if (mProgram != null && !mProgram.isProgramBuilt()) {
            mProgram.buildProgram();
            CL.d("GLFrameRenderer :: buildProgram done");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        CL.d("GLFrameRenderer :: onSurfaceCreated");
        mTargetSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        CL.d("GLFrameRenderer :: onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        VideoFrame videoFrame = null;
        try {
            videoFrame = mMediaDataPool.poll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (this) {
            if (videoFrame != null) {
                if (mVideoWidth != videoFrame.width || mVideoHeight != videoFrame.height) {
                    updateVideoSize(videoFrame.width, videoFrame.height);
                    checkInitProgram();
                }
                // reset position, have to be done
                mProgram.buildTextures(videoFrame.getBufferY(), videoFrame.getBufferUV(), mVideoWidth, mVideoHeight);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                mProgram.drawFrame();
            }
        }
        if (videoFrame != null) {
            mMediaDataPool.cacheObject(videoFrame);
        }
    }

    private void updateVideoSize(int w, int h) {
        initProgram(true);
        // 调整比例
        if (mScreenWidth > 0 && mScreenHeight > 0) {
            float f1 = 1f * mScreenHeight / mScreenWidth;
            float f2 = 1f * h / w;
            if (f1 == f2) {
                mProgram.createBuffers(GLProgram420sp.squareVertices);
            } else if (f1 < f2) {
                float widScale = f1 / f2;
                mProgram.createBuffers(new float[]{-widScale, -1.0f, widScale, -1.0f, -widScale, 1.0f, widScale,
                        1.0f,});
            } else {
                float heightScale = f2 / f1;
                mProgram.createBuffers(new float[]{-1.0f, -heightScale, 1.0f, -heightScale, -1.0f, heightScale, 1.0f,
                        heightScale,});
            }
        }
        mVideoWidth = w;
        mVideoHeight = h;
    }

    private void initProgram(boolean useUVBuffer) {
        if (useUVBuffer) {
            mProgram = new GLProgram420sp(mTargetSurfaceView.getContext().getApplicationContext(), 0);
        } else {
            mProgram = new GLProgramYUV(mTargetSurfaceView.getContext().getApplicationContext(), 0);
        }
    }
//
//    public void update(ByteBuffer y, ByteBuffer uv, int w, int h, boolean useUVBuffer) {
//        checkUpdateSize(w, h, useUVBuffer);
//        if (this.y == null) {
//            return;
//        }
//        synchronized (this) {
//            this.y.clear();
//            this.uv.clear();
//            this.y.put(y);
//            this.uv.put(uv);
//        }
//        // request to render
//        mTargetSurfaceView.requestRender();
//    }

//    private void checkUpdateSize(int w, int h, boolean useUVBuffer) {
//        if (w != mVideoWidth || h != mVideoHeight || useUVBuffer != mUseUVBUffer) {
//            update(w, h, useUVBuffer);
//        }
//    }

    public void destroy() {
    }
}
