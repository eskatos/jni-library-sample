package com.example.greeter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class Greeter {

    static {
        try {
            System.loadLibrary("jni-greeter");
        } catch (UnsatisfiedLinkError ex) {
            String libName = "jni-greeter";
            URL url = Greeter.class.getClassLoader().getResource(libFilename(libName));
            try {
                File file = Files.createTempFile("jni", "greeter").toFile();
                file.deleteOnExit();
                file.delete();
                try (InputStream in = url.openStream()) {
                    Files.copy(in, file.toPath());
                }
                System.load(file.getCanonicalPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static String libFilename(String libName) {
        // TODO depend on OS
        return "lib" + libName + ".dylib";
    }

    public native String sayHello(String name);
}
