#include <jni.h>
#include <string>

extern "C" jstring
Java_com_news_utils_KeyHelper_newsApiKey(
        JNIEnv *env,
        jobject ) {
    std::string key = "38219fd255894a0e96d50f0567f63691";
    return env->NewStringUTF(key.c_str());
}