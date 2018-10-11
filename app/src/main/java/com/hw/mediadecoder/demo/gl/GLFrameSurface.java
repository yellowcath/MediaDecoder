package com.hw.mediadecoder.demo.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;


public class GLFrameSurface extends GLSurfaceView {

    public GLFrameSurface(Context context) {
        super(context);
    }

    public GLFrameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
