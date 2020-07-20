package com.example.demo5;

public class JniTest2 {

    static {
        System.loadLibrary("jni_test");
    }

    public static native  String get();
    public static native void set(String str);
}
