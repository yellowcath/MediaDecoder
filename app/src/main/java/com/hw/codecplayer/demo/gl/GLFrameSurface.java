package com.hw.codecplayer.demo.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import com.hw.codecplayer.util.CL;


public class GLFrameSurface extends GLSurfaceView {

    public GLFrameSurface(Context context) {
        super(context);
    }

    public GLFrameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        CL.d("surface onAttachedToWindow()");
        super.onAttachedToWindow();
        // setRenderMode() only takes effectd after SurfaceView attached to window!
        // note that on this mode, surface will not render util GLSurfaceView.requestRender() is
        // called, it's good and efficient -v-
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        CL.d("surface setRenderMode RENDERMODE_WHEN_DIRTY");
    }
}
