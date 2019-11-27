import org.gradle.internal.jvm.Jvm

plugins {
    `cpp-library`
    `java-library`
}

val jniImplementation by configurations.creating

configurations.matching { it.name.startsWith("cppCompile") || it.name.startsWith("nativeLink") || it.name.startsWith("nativeRuntime") }.all {
    extendsFrom(jniImplementation)
}

val jniHeaderDirectory = layout.buildDirectory.dir("jniHeaders")

tasks.compileJava {
    outputs.dir(jniHeaderDirectory)
    options.compilerArgumentProviders.add(CommandLineArgumentProvider { listOf("-h", jniHeaderDirectory.get().asFile.canonicalPath) })
}

library {
    binaries.configureEach {

        compileTask.get().dependsOn(tasks.compileJava)
        compileTask.get().compilerArgs.addAll(jniHeaderDirectory.map { listOf("-I", it.asFile.canonicalPath) })
        compileTask.get().compilerArgs.addAll(compileTask.get().targetPlatform.map {
            listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include") + when {
                it.operatingSystem.isMacOsX -> listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include/darwin")
                it.operatingSystem.isLinux -> listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include/linux")
                else -> emptyList()
            }
        })
    }
}

tasks.test {
    val sharedLib = library.developmentBinary.get() as CppSharedLibrary
    dependsOn(sharedLib.linkTask)
    systemProperty("java.library.path", sharedLib.linkFile.get().asFile.parentFile)
}

tasks.jar {
    from(library.developmentBinary.flatMap { (it as CppSharedLibrary).linkFile })
}
