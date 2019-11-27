plugins {
    java
    application
}

application {
    mainClassName = "com.example.app.Main"
}

dependencies {
    implementation(project(":jni-greeter"))
}
