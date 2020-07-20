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
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TextureRender implements GLSurfaceView.Renderer {
    //private Oval ovalTop, ovalBottom;
    private Context mContext;
    //private Filter filter;
    public TextureRender(Context context) {
        //ovalTop = new Oval(0.0f);
        //ovalBottom = new Oval(3.0f);
        this.mContext = context;
    }

    private String vertexShaderCode=
            "attribute vec4 vPosition;" +
            "attribute vec2 aCoord;" +
            "uniform mat4 vMatrix;" +
            "varying vec2 vCoord;" +
            "void main() {" +
                "gl_Position = vMatrix * vPosition;" +
                "vCoord=aCoord;" +
            "}";
    private String fragmentShaderCode=
            "precision mediump float;" +
            "uniform sampler2D vTexture;" +
                    "uniform vec4 vChangeColor;" +
                    "uniform int vChangeType;" +
            "varying vec2 vCoord;" +
                    "uniform vec2 gPosition;" +
            "void modifyColor(vec4 color){\n" +
            "    color.r=max(min(color.r,1.0),0.0);\n" +
            "    color.g=max(min(color.g,1.0),0.0);\n" +
            "    color.b=max(min(color.b,1.0),0.0);\n" +
            "    color.a=max(min(color.a,1.0),0.0);\n" +
            "}\n" +

            "void main() {" +
                    "vec4 nColor = texture2D(vTexture,vCoord);" +
                    "if(vChangeType==1){ " + //黑白图片
                        "float c = nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;" +
                        "gl_FragColor = vec4(c,c,c,nColor.a);" +
                    "}else if(vChangeType==2){ " +//简单色彩处理，冷暖色调、增加亮度、降低亮度等
                        "vec4 deltaColor=nColor+vec4(vChangeColor);" +
                        "modifyColor(deltaColor);" +
                        "gl_FragColor=deltaColor;" +
                    "}else if(vChangeType==3){\n " + //模糊处理
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x-vChangeColor.r,vCoord.y-vChangeColor.r));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x-vChangeColor.r,vCoord.y+vChangeColor.r));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x+vChangeColor.r,vCoord.y-vChangeColor.r));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x+vChangeColor.r,vCoord.y+vChangeColor.r));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x-vChangeColor.g,vCoord.y-vChangeColor.g));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x-vChangeColor.g,vCoord.y+vChangeColor.g));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x+vChangeColor.g,vCoord.y-vChangeColor.g));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x+vChangeColor.g,vCoord.y+vChangeColor.g));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x-vChangeColor.b,vCoord.y-vChangeColor.b));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x-vChangeColor.b,vCoord.y+vChangeColor.b));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x+vChangeColor.b,vCoord.y-vChangeColor.b));\n" +
                    "            nColor+=texture2D(vTexture,vec2(vCoord.x+vChangeColor.b,vCoord.y+vChangeColor.b));\n" +
                    "            nColor/=13.0;\n" +
                    "            gl_FragColor=nColor;\n" +
                    "}else if(vChangeType==4){  //放大镜效果\n" +
                    "            float dis=distance(vec2(gPosition.x,gPosition.y),vec2(vChangeColor.r,vChangeColor.g));\n" +
                    "            if(dis<vChangeColor.b){\n" +
                    "                nColor=texture2D(vTexture,vec2(vCoord.x/2.0+0.25,vCoord.y/2.0+0.25));\n" +
                    "            }\n" +
                    "            gl_FragColor=nColor;\n" +
                    "}else{" +
                        //" gl_FragColor = texture2D(vTexture,vCoord);" +
                        " gl_FragColor = nColor;" +
                    "}" +
            "}";
    //圆的点
    //float triangleCoords[] = createCylinder(1000,3);
    //float triangleCoords[] = createGlobe(1000,3);
    //float circleCoords[] = createPositions(50,0);

    private final float[] pos={
            -1.0f,1.0f,
            1.0f,1.0f,
            -1.0f,-1.0f,
            1.0f,-1.0f

    };
    private final float[] coord={
            0.0f,0.0f,
            1.0f,0.0f,
            0.0f,1.0f,
            1.0f,1.0f,
    };

    //float[] color = { 0.299f,0.587f,0.114f,1.0f}; //白色
    Filter filter = Filter.MAGN;
    //float[] color = { 0.299f,0.587f,0.114f,1.0f}; //白色
    float[] color = filter.getData(); //白色
    //设置颜色
    /*float color[] = {
            0.0f, 1.0f, 0.0f, 1.0f ,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };*/
    private FloatBuffer vertexBuffer ;
    private FloatBuffer coordBuffer ;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;
    private int COORDS_PER_VERTEX = 2;
    private int vertexStride = COORDS_PER_VERTEX*4;
    private final int vertexCount = pos.length / COORDS_PER_VERTEX;
    //储存投影矩阵
    float[] mProjectMatrix = new float[16];
    float[] mViewMatrix = new float[16];
    float[] mMVPMatrix = new float[16];
    private int n = 4;
    float[] gPosition = {0.5f,0.5f};


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //开启深度测试
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //将背景设置为灰色
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);

        //申请底层空间
        ByteBuffer bb = ByteBuffer.allocateDirect(pos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(pos);
        vertexBuffer.position(0);

        ByteBuffer aa = ByteBuffer.allocateDirect(coord.length * 4);
        aa.order(ByteOrder.nativeOrder());
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        coordBuffer = aa.asFloatBuffer();
        coordBuffer.put(coord);
        coordBuffer.position(0);


        /*ByteBuffer dd = ByteBuffer.allocateDirect(
                color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);*/

        /*ByteBuffer cc = ByteBuffer.allocateDirect(
                index.length * 2);
        cc.order(ByteOrder.nativeOrder());
        indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);*/

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);

        /*ovalBottom.onSurfaceCreated(gl,config);
        ovalTop.onSurfaceCreated(gl,config);*/
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
        Bitmap mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_cat);
        int w=mBitmap.getWidth();
        int h=mBitmap.getHeight();
        float sWH=w/(float)h;
        //计算宽高比
        float sWidthHeight=(float)width/height;
        Log.d("TextureRender", "sWidthHeight:" + sWidthHeight+",sWH:"+sWH+",sWidthHeight*sWH"+sWidthHeight*sWH+",sWidthHeight/sWH:"+sWidthHeight/sWH);
        if(width>height){
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight*sWH,sWidthHeight*sWH, -1,1, 3, 7);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight/sWH,sWidthHeight/sWH, -1,1, 3, 7);
            }
        }else{
            if(sWH>sWidthHeight){
                //Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1/sWidthHeight*sWH, 1/sWidthHeight*sWH,3, 7);
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1,3, 7);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH/sWidthHeight, sWH/sWidthHeight,3, 7);
            }
        }

        //设置透视投影
        //Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight, sWidthHeight, -1,1,3, 6);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 6.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);

        //这里增加了一个旋转是为了让正方体动起来，需要设置render模式setRenderMode(RENDERMODE_CONTINUOUSLY)
        //Matrix.rotateM(mMVPMatrix, 0, mMVPMatrix, 0,1,1,1,0);

        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler= GLES20.glGetUniformLocation(mProgram,"vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);

        //获取顶点着色器的vPosition成员句柄
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //允许使用顶点坐标数组
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据

       /* GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, cirlceBuffer);*/
        //第一个参数顶点属性的索引值
        // 第二个参数顶点属性的组件数量。必须为1、2、3或者4，如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
        // 第三个参数数组中每个组件的数据类型
        // 第四个参数指定当被访问时，固定点数据值是否应该被归一化（GL_TRUE）或者直接转换为固定点值（GL_FALSE）
        // 第五个参数指定连续顶点属性之间的偏移量，这里由于是三个点 每个点4字节（float） 所以就是 3*4
        // 第六个参数前面的顶点坐标数组

        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        /*//获取片元着色器的vColor成员的句柄
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);*/

        //获取片元着色器的vColor成员的句柄
        int mColorHandle = GLES20.glGetAttribLocation(mProgram, "aCoord");
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle,COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,false,
                8,coordBuffer);

        //
        int vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        GLES20.glUniform1i(vTexture, 0);
        texture();


        //获取片元着色器的vColor成员的句柄
        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        //获取片元着色器的vColor成员的句柄
        int typeHandle = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        //设置绘制三角形的颜色
        Log.d("weikang","filter.getType():"+filter.getType());
        GLES20.glUniform1i(typeHandle, filter.getType());

        //获取片元着色器的vColor成员的句柄
        int gPositionHandle = GLES20.glGetUniformLocation(mProgram, "gPosition");
        //设置绘制三角形的颜色
        //Log.d("weikang","filter.getType():"+filter.getType());
        GLES20.glUniform2fv(gPositionHandle,1,gPosition ,0);


        //绘制三角形
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);


        //获取片元着色器的vColor成员的句柄
        /*int mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle,4,
                GLES20.GL_FLOAT,false,
                0,colorBuffer);*/

        //索引法绘制正方体
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length, GLES20.GL_UNSIGNED_SHORT,indexBuffer);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        /*ovalBottom.setMatrix(mMVPMatrix);
        ovalBottom.onDrawFrame(gl);
        ovalTop.setMatrix(mMVPMatrix);
        ovalTop.onDrawFrame(gl);*/
    }

    int[] texture = new int[4];
    private void texture(){
        //第一个参数是生成纹理数量因为定义的数组长度为1所以这里也是1.可以根据需要增加。
        GLES20.glGenTextures(4, texture, 0);
        //
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_cat),0);
    }


    /*private float[]  createPositions(int n, float height){
        ArrayList<Float> data=new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(height);
        float angDegSpan=360f/n;
        float radius = 1.0f;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(0.0f);
        }
        float[] f=new float[data.size()];
        for (int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }

    private float[]  createCylinder(int n, float height){
        ArrayList<Float> pos=new ArrayList<>();
        float angDegSpan=360f/n;
        float radius = 1.0f;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            pos.add((float) (radius*Math.sin(i*Math.PI/180f)));
            pos.add((float)(radius*Math.cos(i*Math.PI/180f)));
            pos.add(height);
            pos.add((float) (radius*Math.sin(i*Math.PI/180f)));
            pos.add((float)(radius*Math.cos(i*Math.PI/180f)));
            pos.add(0.0f);
        }
        float[] d=new float[pos.size()];
        for (int i=0;i<d.length;i++){
            d[i]=pos.get(i);
        }
        return d;
    }

    private float[]  createGlobe(int n, float height){
        ArrayList<Float> data=new ArrayList<>();
        float r1,r2;
        float h1,h2;
        float sin,cos;
        int step = 1;
        for(float i=-90;i<90+step;i+=step){
            r1 = (float)Math.cos(i * Math.PI / 180.0);
            r2 = (float)Math.cos((i + step) * Math.PI / 180.0);
            h1 = (float)Math.sin(i * Math.PI / 180.0);
            h2 = (float)Math.sin((i + step) * Math.PI / 180.0);
            // 固定纬度, 360 度旋转遍历一条纬线
            float step2=step*2;
            for (float j = 0.0f; j <360.0f+step; j +=step2 ) {
                cos = (float) Math.cos(j * Math.PI / 180.0);
                sin = -(float) Math.sin(j * Math.PI / 180.0);

                data.add(r2 * cos);
                data.add(h2);
                data.add(r2 * sin);
                data.add(r1 * cos);
                data.add(h1);
                data.add(r1 * sin);
            }
        }
        float[] f=new float[data.size()];
        for(int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }*/


}
