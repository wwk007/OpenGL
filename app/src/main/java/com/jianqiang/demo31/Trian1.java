package com.jianqiang.demo31;

/*public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyGLsurfaceView view = new MyGLsurfaceView(this);
        //renderer:渲染器
        view.setRenderer(new MyRenderer());
        //设置视图
        setContentView(view);
    }


    class MyGLsurfaceView extends GLSurfaceView {
        public MyGLsurfaceView(Context context) {
            super(context);

        }

        public MyGLsurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);

        }

    }

    //自定义渲染器,最重点的
    class MyRenderer implements android.opengl.GLSurfaceView.Renderer{
        //表层创建时
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //设置背景色,透明度1为完全不透明
            gl.glClearColor(0, 0, 0, 1);
            //启用客户端状态,启用顶点缓冲区
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        }

        //表层size改变时，即画面的大小改变时调用
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //设置视口,输出画面的区域,在控件的什么区域来输出,x,y是左下角坐标
            gl.glViewport(0, 0, width, height);
            float ratio =(float) width /(float) height;

            //矩阵模式,投影矩阵,openGL基于状态机
            gl.glMatrixMode(GL10.GL_PROJECTION);
            //加载单位矩阵
            gl.glLoadIdentity();
            //平截头体
            gl.glFrustumf(-1f, 1f, -ratio, ratio, 3, 7);
        }

        //绘图
        @Override
        public void onDrawFrame(GL10 gl) {
            //清除颜色缓冲区
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

            //模型视图矩阵
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            //操作新矩阵要先清0,加载单位矩阵
            gl.glLoadIdentity();
            //眼睛放的位置,eyes,eyey,eyez眼球(相机)的坐标
            //centerx,y,z镜头朝向，眼球的观察点
            //upx,upy,upz:指定眼球向上的向量,眼睛正着看
            GLU.gluLookAt(gl, 0, 0, 5, 0, 0, 0, 0, 1, 0);

            //画三角形
            //三角形的坐标
            float[] coords = {
                    0f,0.5f,0f,
                    -0.5f,-0.5f,0f,
                    0.5f,-0.5f,0f,
            };
            //分配字节缓冲区空间,存放顶点坐标数据,将坐标放在缓冲区中
            ByteBuffer ibb = ByteBuffer.allocateDirect(coords.length * 4);  //直接分配字节的大小
            //设置顺序(本地顺序)
            ibb.order(ByteOrder.nativeOrder());
            //放置顶点坐标数组
            FloatBuffer fbb =  ibb.asFloatBuffer();
            fbb.put(coords);
            //定位指针的位置,从该位置开始读取顶点数据
            ibb.position(0);

            //设置绘图的颜色，红色
            gl.glColor4f(1f, 0f, 0f, 1f);
            //3:3维点,使用三个坐标值表示一个点
            //type:每个点的数据类型
            //stride:0,上,点的跨度
            //ibb:指定顶点缓冲区
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, ibb);
            //绘制三角形数组
            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);   //count以点的数量来算,为3
        }

    }


}*/

/*
private String fragmentShaderCode=
        "precision mediump float;" +
        "uniform sampler2D vTexture;" +
        "uniform int vChangeType;" +
        "uniform vec3 vChangeColor;" +
        "uniform int vIsHalf;" +
        "uniform float uXY;" +
        "varying vec4 gPosition;" +
        "varying vec2 vCoord;" +
        "varying vec4 aPos;" +
        "void modifyColor(vec4 color){\n" +
        "    color.r=max(min(color.r,1.0),0.0);\n" +
        "    color.g=max(min(color.g,1.0),0.0);\n" +
        "    color.b=max(min(color.b,1.0),0.0);\n" +
        "    color.a=max(min(color.a,1.0),0.0);\n" +
        "}\n" +
        "void main(){\n" +
        "    vec4 nColor=texture2D(vTexture,vCoord);\n" +
        "    if(aPos.x>0.0||vIsHalf==0){\n" +
        "        if(vChangeType==1){    //黑白图片\n" +
        "            float c=nColor.r*vChangeColor.r+nColor.g*vChangeColor.g+nColor.b*vChangeColor.b;\n" +
        "            gl_FragColor=vec4(c,c,c,nColor.a);\n" +
        "        }else if(vChangeType==2){    //简单色彩处理，冷暖色调、增加亮度、降低亮度等\n" +
        "            vec4 deltaColor=nColor+vec4(vChangeColor,0.0);\n" +
        "            modifyColor(deltaColor);\n" +
        "            gl_FragColor=deltaColor;\n" +
        "        }else if(vChangeType==3){    //模糊处理\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y-vChangeColor.r));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.r,aCoordinate.y+vChangeColor.r));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y-vChangeColor.r));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.r,aCoordinate.y+vChangeColor.r));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y-vChangeColor.g));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.g,aCoordinate.y+vChangeColor.g));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y-vChangeColor.g));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.g,aCoordinate.y+vChangeColor.g));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y-vChangeColor.b));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x-vChangeColor.b,aCoordinate.y+vChangeColor.b));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y-vChangeColor.b));\n" +
        "            nColor+=texture2D(vTexture,vec2(aCoordinate.x+vChangeColor.b,aCoordinate.y+vChangeColor.b));\n" +
        "            nColor/=13.0;\n" +
        "            gl_FragColor=nColor;\n" +
        "        }else if(vChangeType==4){  //放大镜效果\n" +
        "            float dis=distance(vec2(gPosition.x,gPosition.y/uXY),vec2(vChangeColor.r,vChangeColor.g));\n" +
        "            if(dis<vChangeColor.b){\n" +
        "                nColor=texture2D(vTexture,vec2(aCoordinate.x/2.0+0.25,aCoordinate.y/2.0+0.25));\n" +
        "            }\n" +
        "            gl_FragColor=nColor;\n" +
        "        }else{\n" +
        "            gl_FragColor=nColor;\n" +
        "        }\n" +
        "    }else{\n" +
        "        gl_FragColor=nColor;\n" +
        "    }\n" +
        "}\n";
*/
