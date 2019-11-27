import org.gradle.internal.jvm.Jvm
import javax.inject.Inject

plugins {
    `cpp-library`
    `java-library`
}

val jni by configurations.creating

configurations.matching { it.name.startsWith("cppCompile") || it.name.startsWith("nativeLink") || it.name.startsWith("nativeRuntime") }.all {
    extendsFrom(jni)
}

dependencies {

    jni(project(":native-greeter"))

    testImplementation("junit:junit:4.12")
}

val jniHeaderDirectory = layout.buildDirectory.dir("jniHeaders")
tasks.named<JavaCompile>("compileJava") {
    outputs.dir(jniHeaderDirectory)
    options.compilerArgumentProviders.add(CommandLineArgumentProvider { listOf("-h", jniHeaderDirectory.get().asFile.canonicalPath) })
}

library {
    binaries.configureEach {
        compileTask.get().compilerArgs.addAll(compileTask.get().toolChain.map {
            if (it is Gcc || it is Clang) listOf("--std=c++11")
            else emptyList()
        })
        compileTask.get().dependsOn("compileJava")
        compileTask.get().compilerArgs.addAll(jniHeaderDirectory.map { listOf("-I", it.asFile.canonicalPath) })
        compileTask.get().compilerArgs.addAll(compileTask.get().targetPlatform.map {
            val result = mutableListOf("-I", "${Jvm.current().javaHome.canonicalPath}/include")
            if (it.operatingSystem.isMacOsX) {
                result.addAll(listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include/darwin"))
            } else if (it.operatingSystem.isLinux) {
                result.addAll(listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include/linux"))
            }

            return@map result
        })
    }
}

val buildJniWrapper by tasks.registering {}

tasks.test {
    classpath += files("build/lib/main/debug").builtBy(library.developmentBinary.map { (it as CppSharedLibrary).linkTask })
    systemProperty("java.library.path", classpath.asPath)
}

sourceSets {
    main {
        resources {
            srcDir(files(buildJniWrapper).builtBy(buildJniWrapper))
        }
    }
}
