plugins {
    id("jni-library")
}

dependencies {

    jniImplementation(project(":native-greeter"))

    testImplementation("junit:junit:4.12")
}

library {
    binaries.configureEach {
        compileTask.get().compilerArgs.addAll(compileTask.get().toolChain.map {
            if (it is Gcc || it is Clang) listOf("--std=c++11")
            else emptyList()
        })
    }
}
