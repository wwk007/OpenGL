package com.jianqiang.demo31;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.security.PublicKey;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DBitmapGLRender implements GLSurfaceView.Renderer {
    private Context mContext;
    //顶点坐标
    private final float[] pos = {
            -1.0f,1.0f,
            1.0f,1.0f,
            -1.0f,-1.0f,
            1.0f,-1.0f
    };
    //纹理坐标
    private final float[] coord = {
            0.0f,0.0f,
            1.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
    };

    private String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "attribute vec2 aCoord;" +
            "uniform mat4 vMatrix;" +
            "varying vec2 vCoord;" +
            "void main() {" +
            "gl_Position = vMatrix*vPosition;" +
            "vCoord = aCoord;" +
            "}";

    private String fragmentShaderCode =
            "precision mediump float;" +
            "uniform sampler2D vTexture;" +
            "varying vec2 vCoord;" +
            "void main() {" +
            "  gl_FragColor = texture2D(vTexture,vCoord);" +
            "}";

    private FloatBuffer vertexBuffer;
    private int mProgram;
    private FloatBuffer coordBuffer ;
    private ShortBuffer indexBuffer;
    private float[] mMVPMatrix;

    public DBitmapGLRender(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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
       /* int[] linsStatus = new int[1];
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


        //透视矩阵
        //存储生成矩阵元素的float[]类型数组
        //填充起始偏移量
        //near面的left,right,bottom,top
        //near面,far面与视点的距离

        Matrix.orthoM(mPMatrix, 0, -ratio, ratio, -1,1,3, 6);

        //存储生成矩阵元素的float[]类型数组
        //填充起始偏移量
        //摄像机位置X,Y,Z坐标
        //观察目标X,Y,Z坐标
        //up向量在X,Y,Z上的分量,也就是相机上方朝向，upY=1朝向手机上方，upX=1朝向手机右侧，upZ=1朝向与手机屏幕垂直
        Matrix.setLookAtM(mVMatrix, 0, 0,0,6, 0,0,0,0,1,0);

        //以上两个方法只能得到矩阵并不能使其生效
        //下面通过矩阵计算得到最终想要的矩阵
        //存放结果的总变换矩阵
        //结果矩阵偏移量
        //左矩阵
        //左矩阵偏移量
        //右矩阵
        //右矩阵偏移量
        Matrix.multiplyMM(mMVPMatrix, 0,mPMatrix, 0, mVMatrix, 0);


        //当大小改变时重置视区大小
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清空缓冲区，与  GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);对应
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

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
        GLES20.glVertexAttribPointer(mPositionHandle, 2,
                GLES20.GL_FLOAT, false,
                8, vertexBuffer);

        /*//获取片元着色器的vColor成员的句柄
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);*/
        int aColorHandler = GLES20.glGetAttribLocation(mProgram, "aCoord");
        GLES20.glEnableVertexAttribArray(aColorHandler);
        GLES20.glVertexAttribPointer(aColorHandler, 2, GLES20.GL_FLOAT, false, 8, coordBuffer);

        int vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        GLES20.glUniform1i(vTexture, 0);
        texture();
        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //索引法绘制正方体
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT,indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    int[] texture = new int[1];
    private void texture() {
        //第一个参数是生成纹理数量因为定义的数组长度为1所以这里也是1.可以根据需要增加。
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mouse),0);
    }
}
