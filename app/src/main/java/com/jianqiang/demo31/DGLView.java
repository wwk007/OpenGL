package com.jianqiang.demo31;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DGLView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private DGLRender dglRender;

    public DGLView(Context context) {
        super(context);
        init();
    }

    public DGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        dglRender.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        dglRender.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        dglRender.onDrawFrame(gl);
    }

    private void init() {
        //初始化Renderer
        dglRender = new DGLRender();
        //设置EGLContext为2.0
        setEGLContextClientVersion(2);
        //设置render，绘制全靠它
        setRenderer(this);
        //设置render模式为只在绘制数据发生改变时才绘制view
        //此设置会阻止绘制GLSurfaceView的帧，直到你调用了requestRender()，这样会非常高效。
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

}
