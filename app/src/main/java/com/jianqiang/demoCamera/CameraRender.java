package com.jianqiang.demoCamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.jianqiang.demo31.LogUtil;
import com.jianqiang.demo31.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.security.PublicKey;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_TEXTURE0;

public class CameraRender implements GLSurfaceView.Renderer {
    private Context mContext;
    //顶点坐标
    private final float[] pos = {
            -1.0f,1.0f,0.0f,
            1.0f,1.0f,0.0f,
            -1.0f,-1.0f,0.0f,
            1.0f,-1.0f,0.0f
    };
    //纹理坐标
    private final float[] coord = {
            0.0f,0.0f,
            1.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
    };

    private FloatBuffer vertexBuffer;
    private int mProgram;
    private FloatBuffer coordBuffer ;
    private ShortBuffer indexBuffer;
    private float[] mMVPMatrix;
    //接收相机数据的纹理
    private int[] textureId = new int[1];
    //接收相机数据的 SurfaceTexture
    public SurfaceTexture surfaceTexture;

    public CameraRender(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        LogUtil.d("onSurfaceCreated");
        //创建纹理对象
        GLES20.glGenTextures(textureId.length, textureId, 0);
        //将纹理对象绑定到srufaceTexture
        LogUtil.d("onSurfaceCreated:"+textureId[0]);
        surfaceTexture = new SurfaceTexture(textureId[0]);

        //将背景设置为灰色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f,1.0f);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //申请底层空间
        //创建一个顶点坐标Buffer，一个float为4字节所以这里需要
        ByteBuffer bb = ByteBuffer.allocateDirect(pos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转为FloatBuffer,用以传入openGL ES程序
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(pos);
        vertexBuffer.position(0);


        ByteBuffer cbyteBuffer = ByteBuffer.allocateDirect(coord.length * 4);
        cbyteBuffer.order(ByteOrder.nativeOrder());
        coordBuffer = cbyteBuffer.asFloatBuffer();
        coordBuffer.put(coord);
        coordBuffer.position(0);

        //装载顶点着色器和片元着色器，从source
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,loadShaderSource(R.raw.camra_vertex));
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER,loadShaderSource(R.raw.camra_fragment));

        //创建Opengl程序，获取程序句柄，为了方便onDrawFrame方法使用所以声明为成员变量
        mProgram = GLES20.glCreateProgram();
        LogUtil.d(",vertexShader:"+vertexShader+",fragmentShader:"+fragmentShader+",mProgram:"+mProgram);
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);
       /* int[] linsStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linsStatus, 0);
        if(linsStatus[0] != GLES20.GL_TRUE) {
            Log.d("weikang", "link program error, vertexShader:"+vertexShader+",fragmentShader:"+fragmentShader+",mProgram:"+mProgram);
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }*/
    }

    private String loadShaderSource(int resId) {
        StringBuilder res = new StringBuilder();
        InputStream is = mContext.getResources().openRawResource(resId);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String nextLine;
        try {
            while ((nextLine = bufferedReader.readLine()) != null){
                res.append(nextLine);
                res.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
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
        LogUtil.d("onSurfaceChanged");
        //计算屏幕宽高比
        float ratio = (float)width/height;
        //储存投影矩阵
        float[] mPMatrix = new float[16];
        //储存相机位置矩阵
        float[] mVMatrix = new float[16];
        //最终得到的矩阵
        mMVPMatrix = new float[16];
        /*Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mouse);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float ratioB = (float)w/h;

        if(width>height){
            if(ratioB>ratio){
                Matrix.orthoM(mPMatrix, 0, -ratio*ratioB, ratio*ratioB,-1,1,3, 6);
            }else{
                Matrix.orthoM(mPMatrix, 0, -ratioB/ratio, ratioB/ratio, -1,1,3, 6);
            }
        }else{
            if(ratioB>ratio){
                Matrix.orthoM(mPMatrix, 0, -1,1, -1/ratioB*ratio, 1/ratioB*ratio,3, 6);
            }else{
                Matrix.orthoM(mPMatrix, 0, -1,1, -ratioB/ratio, ratioB/ratio,3, 6);
            }
        }*/
        Matrix.orthoM(mPMatrix, 0, -ratio, ratio, -1,1,3, 6);
        Matrix.setLookAtM(mVMatrix, 0, 0,0,6, 0,0,0,0,1,0);
        Matrix.multiplyMM(mMVPMatrix, 0,mPMatrix, 0, mVMatrix, 0);

        /*float[] projection=new float[16];
        float[] camera=new float[16];
        float[] matrix=new float[16];

        float sWhView=(float)viewWidth/viewHeight;
        float sWhImg=(float)imgWidth/imgHeight;
        if(sWhImg>sWhView){
            Matrix.orthoM(projection,0,-sWhView/sWhImg,sWhView/sWhImg,-1,1,1,3);
        }else{
            Matrix.orthoM(projection,0,-1,1,-sWhImg/sWhView,sWhImg/sWhView,1,3);
        }
        Matrix.setLookAtM(camera,0,0,0,1,0,0,0,0,1,0);
        Matrix.multiplyMM(matrix,0,projection,0,camera,0);*/


        //当大小改变时重置视区大小
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        LogUtil.d("onDrawFrame");
        //srufaceTexture 获取新的纹理数据
        surfaceTexture.updateTexImage();
        //清空缓冲区，与  GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);对应
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        int vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(vMatrix, 1 ,false, mMVPMatrix, 0);


        //获取顶点着色器的vPosition成员句柄
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //允许使用顶点坐标数组
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int aColorHandler = GLES20.glGetAttribLocation(mProgram, "aCoord");
        GLES20.glEnableVertexAttribArray(aColorHandler);
        GLES20.glVertexAttribPointer(aColorHandler, 2, GLES20.GL_FLOAT, false, 0, coordBuffer);

        //绑定0号纹理单元纹理
        GLES20.glActiveTexture(GL_TEXTURE0);
        //将纹理放到当前单元的 GL_TEXTURE_BINDING_EXTERNAL_OES 目标对象中
        texture();

        //将片段着色器的纹理属性值（s_texture）设置为 0 号单元
        int vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        GLES20.glUniform1i(vTexture, 0);
        //texture();
        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        //索引法绘制正方体
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT,indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(aColorHandler);
    }


    private int texture() {
        //int[] texture = new int[1];
        //GLES20.glGenTextures(textureId.length, textureId, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        LogUtil.d(textureId[0]+"");
        return textureId[0];
    }

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }

}
