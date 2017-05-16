package com.hw.codecplayer.demo.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import com.hw.codecplayer.demo.PlayDemo;
import com.hw.codecplayer.util.CL;
import com.hw.codecplayer.util.MediaFramePool;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;


public class GLFrameRenderer implements Renderer {

    private GLSurfaceView mTargetSurface;
    private IGLProgram mProgram;
    private int mScreenWidth, mScreenHeight;
    private int mVideoWidth, mVideoHeight;
    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;
    private ByteBuffer uv;
    private PlayDemo playDemo;
    public GLFrameRenderer(GLSurfaceView surface, DisplayMetrics dm, MediaFramePool pool) {
        mTargetSurface = surface;
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        playDemo = new PlayDemo(this, pool);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        playDemo.start();

        CL.d("GLFrameRenderer :: onSurfaceCreated");
        if (!mProgram.isProgramBuilt()) {
            mProgram.buildProgram();
            CL.d("GLFrameRenderer :: buildProgram done");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        CL.d("GLFrameRenderer :: onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (y != null) {
                // reset position, have to be done
                y.position(0);
                if (uv == null) {
                    u.position(0);
                    v.position(0);
                    mProgram.buildTextures(y, u, v, mVideoWidth, mVideoHeight);
                } else {
                    uv.position(0);
                    mProgram.buildTextures(y, uv, mVideoWidth, mVideoHeight);
                }
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                mProgram.drawFrame();
            }
        }
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void update(int w, int h,boolean useUVBuffer) {
        CL.d("INIT E");
        if (mProgram == null) {
            initProgram(useUVBuffer);
        }
        if (w > 0 && h > 0) {
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
            // 初始化容器
            if (w != mVideoWidth && h != mVideoHeight) {
                this.mVideoWidth = w;
                this.mVideoHeight = h;
                int yarraySize = w * h;
                synchronized (this) {
                    y = ByteBuffer.allocate(yarraySize);
                    if (useUVBuffer) {
                        uv = ByteBuffer.allocate(yarraySize / 2);
                    } else {
                        u = ByteBuffer.allocate(yarraySize / 4);
                        v = ByteBuffer.allocate(yarraySize / 4);
                    }
                }
            }
        }
    }

    private void initProgram(boolean useUVBuffer) {
        if (useUVBuffer) {
            mProgram = new GLProgram420sp(mTargetSurface.getContext().getApplicationContext(), 0);
        } else {
            mProgram = new GLProgramYUV(mTargetSurface.getContext().getApplicationContext(), 0);
        }
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     *
     * @param y
     * @param u
     * @param v
     */
    public void update(ByteBuffer y, ByteBuffer u, ByteBuffer v) {
        if (this.y == null) {
            return;
        }
        synchronized (this) {
            this.y.clear();
            this.u.clear();
            this.v.clear();
            this.y.put(y);
            this.u.put(u);
            this.v.put(v);
        }
        // request to render
        mTargetSurface.requestRender();
    }

    public void update(ByteBuffer y, ByteBuffer uv) {
        if (this.y == null) {
            return;
        }
        synchronized (this) {
            this.y.clear();
            this.uv.clear();
            this.y.put(y);
            this.uv.put(uv);
        }
        // request to render
        mTargetSurface.requestRender();
    }


}
