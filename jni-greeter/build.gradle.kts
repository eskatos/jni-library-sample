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


abstract class GenerateJniHeaders @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    // javah ../../Greeter.java => .h

    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:InputFiles
    abstract val nativeClasses: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun action() {
        val jvm = org.gradle.internal.jvm.Jvm.current()
        val javac = jvm.javacExecutable
        val out = outputDir.get().asFile
        out.mkdirs()
        execOps.exec {
            setExecutable(javac)
            args(
                nativeClasses.files.joinToString(" ") { it.canonicalPath },
                "-cp", classpath.asPath,
                "-h", out.canonicalPath,
                "-d", temporaryDir.canonicalPath // TODO prevent spurious .class generation
            )
        }
    }
}

val generateJniHeaders by tasks.registering(GenerateJniHeaders::class) {
    classpath.from(sourceSets.main.map { it.runtimeClasspath })
    nativeClasses.from(sourceSets.main.map { it.allJava })
    outputDir.set(layout.buildDirectory.dir(name))
}


library {
    binaries.configureEach {
        compileTask.get().compilerArgs.addAll(compileTask.get().toolChain.map {
            if (it is Gcc || it is Clang) {
                return@map listOf("--std=c++11")
            }
            return@map listOf()
        })
        compileTask.get().compilerArgs.addAll(generateJniHeaders.map { listOf("-I", it.outputDir.get().asFile.canonicalPath) })
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

sourceSets {
    main {
        resources {
            srcDir(files(buildJniWrapper).builtBy(buildJniWrapper))
        }
    }
}
