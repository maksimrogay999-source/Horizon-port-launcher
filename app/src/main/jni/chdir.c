#include <jni.h>
#include <unistd.h>

JNIEXPORT jboolean JNICALL
Java_hnm_horizon_launcher_MainActivity_nativeChdir(JNIEnv *env, jobject thiz, jstring path) {
    const char *nativePath = (*env)->GetStringUTFChars(env, path, 0);
    int result = chdir(nativePath);
    (*env)->ReleaseStringUTFChars(env, path, nativePath);
    return result == 0 ? JNI_TRUE : JNI_FALSE;
}