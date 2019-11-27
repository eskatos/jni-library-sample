import javax.inject.Inject

plugins {
    id("java-library")
}

val jni by configurations.creating

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

val buildJniWrapper by tasks.registering {}

sourceSets {
    main {
        resources {
            srcDir(files(buildJniWrapper).builtBy(buildJniWrapper))
        }
    }
}
