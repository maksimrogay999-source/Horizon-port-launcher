#include <jni.h>
#include <android/native_activity.h>
#include <dlfcn.h>
#include <android/log.h>

#define LOG_TAG "HorizonLoader"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Путь к симлинку на библиотеку, который мы создаем в корне игры (filesDir)
#define HORIZON_LIB_PATH "/data/data/hnm.horizon.launcher/files/libhorizon.so"

void ANativeActivity_onCreate(ANativeActivity* activity, void* savedState, size_t savedStateSize) {
    LOGI("Loading Horizon from: %s", HORIZON_LIB_PATH);

    void* handle = dlopen(HORIZON_LIB_PATH, RTLD_NOW | RTLD_LOCAL);
    if (!handle) {
        LOGE("dlopen failed: %s", dlerror());
        return;
    }

    void (*realOnCreate)(ANativeActivity*, void*, size_t) = 
        (void (*)(ANativeActivity*, void*, size_t)) dlsym(handle, "ANativeActivity_onCreate");
    if (!realOnCreate) {
        LOGE("dlsym failed: %s", dlerror());
        return;
    }

    LOGI("Calling real ANativeActivity_onCreate");
    realOnCreate(activity, savedState, savedStateSize);
}