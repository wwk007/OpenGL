package com.jianqiang.demo31;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.demo5.JniTest2;
import com.jianqiang.demo31.R;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.test);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JniTest2.set("tom");
                textView.setText(JniTest2.get());
                int[] a = {0,1};
            }
        });

        //DBitmapGLRender render = new DBitmapGLRender(this);
        //SquareRender render1 = new SquareRender();
        //TextureRender render = new TextureRender(this);
        DisplaceRender render = new DisplaceRender(this);
        //Cube cube = new Cube();
        glSurfaceView = findViewById(R.id.glsurface);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(render);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}

