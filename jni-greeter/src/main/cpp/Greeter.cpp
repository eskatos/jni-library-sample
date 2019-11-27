#include "com_example_greeter_Greeter.h"

#include <cstring>
#include <cstdlib>

JNIEXPORT jstring JNICALL Java_com_example_greeter_Greeter_sayHello(JNIEnv * env, jobject self, jstring name) {
    char *buf = (char*)std::malloc(10);
    std::strcpy(buf, "123456789");
    auto jstrBuf = env->NewStringUTF(buf);
    return jstrBuf;
}