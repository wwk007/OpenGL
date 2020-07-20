package com.jianqiang.demo31;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.Shape;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class Cube implements GLSurfaceView.Renderer {
    private VaryTools tools;

    public Cube(){
        tools = new VaryTools();
    }

    private final float[] triangleCoords = {
            /*  0.5f,0.5f,0.0f,
              0.5f,-0.5f,0.0f,
              -0.5f,0.5f,0.0f,
              -0.5f,-0.5f,0.0f*/

            /* -0.5f,0.5f,0.0f,
             -0.5f,-0.5f,0.0f,
             0.5f,0.5f,0.0f,
             0.5f,-0.5f,0.0f*/

            /*-0.5f,0.5f,0.0f,
            -0.5f,-0.5f,0.0f,
            0.5f,-0.5f,0.0f,
            -0.5f,0.5f,0.0f,
            0.5f,-0.5f,0.0f,
            0.5f,0.5f,0.0f*/

            -1.0f,1.0f,1.0f,    //正面左上
            -1.0f,-1.0f,1.0f,   //正面左下
            1.0f,-1.0f,1.0f,    //正面右下
            1.0f,1.0f,1.0f,     //正面右上
            -1.0f,1.0f,-1.0f,    //反面左上
            -1.0f,-1.0f,-1.0f,   //反面左下
            1.0f,-1.0f,-1.0f,    //反面右下
            1.0f,1.0f,-1.0f     //反面右上
    };


    //索引坐标
    final short index[]={
            0,1,3,1,2,3,    //正面
            3,2,7,2,6,7,    //右面
            7,6,4,6,4,5,    //后面
            4,5,0,5,1,0,    //左面
            1,2,5,2,6,5,    //下面
            0,3,4,3,7,4     //上面
    };


    private FloatBuffer vertexBuffer;
    private int mProgram;
    private static final int COORDS_PER_VERTEX = 3;
    /**顶点之间的偏移量*/
    private int vertexStride = COORDS_PER_VERTEX * 4;
    // 每个顶点四个字节
    //顶点个数
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;

    private String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;" +
                    "varying vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "vColor = aColor;" +
                    "}";

    private String fragmentShaderCode =
            "precision mediump float;" +
                    "varying  vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final float[] color = {
            /*1.0f,0.0f,0.0f,1.0f,
            0.0f,1.0f,0.0f,1.0f,
            0.0f,0.0f,1.0f,1.0f,*/

            0f,0.5f,0f,1f,
            0.5f,0f,0f,1f,
            0f,0f,0.5f,1f,
            0f,0.5f,0.5f,1f,
            0.5f,0.5f,0f,1f,
            0.5f,0f,0.5f,1f,
            0.5f,0.5f,0.5f,1f,
            0f,0f,0f,1f,
    };
    private FloatBuffer coordBuffer ;
    private ShortBuffer indexBuffer;
    private float[] mMVPMatrix;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //启用深度测试
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //将背景设置为灰色
        //
         //GLES20.glClearColor(0.5f, 0.5f, 0.5f,1.0f);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //申请底层空间
        //创建一个顶点坐标Buffer，一个float为4字节所以这里需要
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转为FloatBuffer,用以传入openGL ES程序
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);


        ByteBuffer cbyteBuffer = ByteBuffer.allocateDirect(color.length * 4);
        cbyteBuffer.order(ByteOrder.nativeOrder());
        coordBuffer = cbyteBuffer.asFloatBuffer();
        coordBuffer.put(color);
        coordBuffer.position(0);

        ByteBuffer ibyteBuffer = ByteBuffer.allocateDirect(index.length*2);
        ibyteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer  = ibyteBuffer.asShortBuffer();
        indexBuffer .put(index);
        indexBuffer .position(0);


        //装载顶点着色器和片元着色器，从source
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        //创建Opengl程序，获取程序句柄，为了方便onDrawFrame方法使用所以声明为成员变量
        mProgram = GLES20.glCreateProgram();
        Log.d("weikang", "link program error, vertexShader:"+vertexShader+",fragmentShader:"+fragmentShader+",mProgram:"+mProgram);
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
       /*int[] linsStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linsStatus, 0);
        if(linsStatus[0] != GLES20.GL_TRUE) {
            Log.d("weikang", "link program error, vertexShader:"+vertexShader+",fragmentShader:"+fragmentShader+",mProgram:"+mProgram);
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }*/
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
/*        //计算屏幕宽高比
        float ratio = (float)width/height;
//储存投影矩阵
        float[] mPMatrix = new float[16];
//储存相机位置矩阵
        float[] mVMatrix = new float[16];
//最终得到的矩阵
        mMVPMatrix = new float[16];

        Matrix.frustumM(mPMatrix, 0, -ratio, ratio, -1,1,3, 20);
        //存储生成矩阵元素的float[]类型数组
        //填充起始偏移量
        //摄像机位置X,Y,Z坐标
        //观察目标X,Y,Z坐标
        //up向量在X,Y,Z上的分量,也就是相机上方朝向，upY=1朝向手机上方，upX=1朝向手机右侧，upZ=1朝向与手机屏幕垂直
        Matrix.setLookAtM(mVMatrix, 0, 5,5,10, 0,0,0,0,1,0);

//以上两个方法只能得到矩阵并不能使其生效
        //下面通过矩阵计算得到最终想要的矩阵
        //存放结果的总变换矩阵
        //结果矩阵偏移量
        //左矩阵
        //左矩阵偏移量
        //右矩阵
        //右矩阵偏移量
        Matrix.multiplyMM(mMVPMatrix, 0,mPMatrix, 0, mVMatrix, 0);*/


        //当大小改变时重置视区大小
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清空缓冲区，与  GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);对应
        //GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //这里增加了一个旋转是为了让正方体动起来，需要设置render模式setRenderMode(RENDERMODE_CONTINUOUSLY)
        //Matrix.rotateM(mMVPMatrix, 0, mMVPMatrix, 0,1,1,1,0);

        int vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(vMatrix, 1 ,false, mMVPMatrix, 0);

        //获取顶点着色器的vPosition成员句柄
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //允许使用顶点坐标数组
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        //第一个参数顶点属性的索引值
        // 第二个参数顶点属性的组件数量。必须为1、2、3或者4，如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
        // 第三个参数数组中每个组件的数据类型
        // 第四个参数指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）
        // 第五个参数指定连续顶点属性之间的偏移量，这里由于是三个点 每个点4字节（float） 所以就是 3*4
        // 第六个参数前面的顶点坐标数组
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        //获取片元着色器的vColor成员的句柄
        /*int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);*/
        int aColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor");
        GLES20.glEnableVertexAttribArray(aColorHandler);
        GLES20.glVertexAttribPointer(aColorHandler, 4, GLES20.GL_FLOAT, false, 0, coordBuffer);

        //绘制图形
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        //索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT,indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }

    public void setMatrix(float[] mMVPMatrix) {
        this.mMVPMatrix = mMVPMatrix;
    }
}
