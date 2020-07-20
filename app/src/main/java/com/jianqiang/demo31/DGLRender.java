package com.jianqiang.demo31;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by WT on 2018/4/8.
 */
public class DGLRender implements GLSurfaceView.Renderer {
    private String TAG = "weikang";

    //顶点着色器代码
    private final String vertex = "" +
            "attribute vec4 vPosition;" +
            "void main(){" +
            "gl_Position=vPosition;" +
            "}";

    //片元着色器代码
    private final String fragment = "" +
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main(){" +
            "gl_FragColor=vColor;" +
            "}";

    //顶点坐标
    private final float[] pos = {
            -0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
    };

    //颜色值
    private final float[] colors = {
            1.0f, 0.0f, 0.0f, 1.0f
    };

    //GL程序
    int program;
    // 顶点坐标Buffer
    FloatBuffer vertexBuffer;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为灰色，这里只是设置，并没有立即生效
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //创建一个顶点坐标Buffer，一个float为4字节所以这里需要
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(pos.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(pos);
        vertexBuffer.position(0);
        //装载顶点着色器和片元着色器，从source
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        //创建Opengl程序，获取程序句柄，为了方便onDrawFrame方法使用所以声明为成员变量
        program = GLES20.glCreateProgram();
        Log.d(TAG,"vertexShader:"+vertexShader+",fragmentShader:"+fragmentShader+",program:"+program);
        //激活着色器
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        //链接程序
        GLES20.glLinkProgram(program);

    }

    /**
     * 装载着色器从资源代码，需要检测是否生成成功，暂时不检测
     *
     * @param type   着色器类型
     * @param source 着色器代码源
     * @return 返回着色器句柄
     */
    private int loadShader(int type, String source) {
        int shader = 0;
        shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //当大小改变时重置视区大小
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清空缓冲区，与  GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);对应
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        //使用OpenGL程序
        GLES20.glUseProgram(program);
        //获取顶点着色器变量vPosition
        int vPositionHandler = GLES20.glGetAttribLocation(program, "vPosition");
        //允许使用顶点坐标数组
        GLES20.glEnableVertexAttribArray(vPositionHandler);
        //第一个参数顶点属性的索引值
        // 第二个参数顶点属性的组件数量。必须为1、2、3或者4，如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
        // 第三个参数数组中每个组件的数据类型
        // 第四个参数指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）
        // 第五个参数指定连续顶点属性之间的偏移量，这里由于是三个点 每个点4字节（float） 所以就是 3*4
        // 第六个参数前面的顶点坐标数组
        GLES20.glVertexAttribPointer(vPositionHandler, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        //获取片元着色器变量vColor
        int vColor = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(vColor, 1, colors, 0);
        //三角形绘制方式
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
        //禁止使用顶点坐标数组
        GLES20.glDisableVertexAttribArray(vPositionHandler);
    }
}