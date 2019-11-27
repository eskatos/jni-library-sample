package com.example.greeter;

public class Greeter {

    static {
        System.loadLibrary("jni-greeter");
    }

    public native String sayHello(String name);
}
