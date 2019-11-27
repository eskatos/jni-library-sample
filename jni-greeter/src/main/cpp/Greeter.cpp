#include "com_example_greeter_Greeter.h"

#include <cstring>

JNIEXPORT jstring JNICALL Java_com_example_greeter_Greeter_sayHello(JNIEnv *, jobject, jstring) {
    char *buf = (char*)malloc(10);
    strcpy(buf, "123456789");
    jstring jstrBuf = (*env)->NewStringUTF(env, buf);
    return jstrBuf;
}