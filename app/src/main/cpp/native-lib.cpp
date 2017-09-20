#include <jni.h>
#include <string>
#include <stdlib.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_jp_ad_iij_nuse_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());
}
