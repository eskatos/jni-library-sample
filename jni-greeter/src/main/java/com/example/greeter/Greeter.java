package com.example.greeter;

public class Greeter {

    static {
        System.loadLibrary("native-greeter");
    }

    public native String sayHello(String name);
}
