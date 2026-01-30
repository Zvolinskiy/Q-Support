#include <jni.h>
#include <vector>
#include <string>

static const uint8_t AES_KEY[] = "Z1a2B3c4D5e6F7g8H9i0J1k2L3m4N5o6";
static const uint8_t AES_IV[]  = "A1B2C3D4E5F6G7H8";
static const uint8_t XOR_MASK = 0xAF;

void apply_xor(uint8_t* data, size_t len) {
    for (size_t i = 0; i < len; i++) {
        data[i] ^= XOR_MASK;
    }
}

void mock_aes_process(uint8_t* data, size_t len, bool encrypt) {
    for (size_t i = 0; i < len; i++) {
        if (encrypt) {
            data[i] = (uint8_t)(data[i] + AES_KEY[i % 32]);
        } else {
            data[i] = (uint8_t)(data[i] - AES_KEY[i % 32]);
        }
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_qsupport_MainActivity_encryptHybrid(JNIEnv* env, jobject obj, jbyteArray data) {
    if (data == NULL) return NULL;

    jsize len = env->GetArrayLength(data);
    jbyte* buffer = env->GetByteArrayElements(data, 0);
    uint8_t* work_ptr = reinterpret_cast<uint8_t*>(buffer);

    mock_aes_process(work_ptr, len, true);
    apply_xor(work_ptr, len);

    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, reinterpret_cast<jbyte*>(work_ptr));
    env->ReleaseByteArrayElements(data, buffer, 0);

    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_qsupport_MainActivity_decryptHybrid(JNIEnv* env, jobject obj, jbyteArray data) {
    if (data == NULL) return NULL;

    jsize len = env->GetArrayLength(data);
    jbyte* buffer = env->GetByteArrayElements(data, 0);
    uint8_t* work_ptr = reinterpret_cast<uint8_t*>(buffer);

    apply_xor(work_ptr, len);
    mock_aes_process(work_ptr, len, false);

    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, reinterpret_cast<jbyte*>(work_ptr));
    env->ReleaseByteArrayElements(data, buffer, 0);

    return result;
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_qsupport_MainActivity_HTNative(JNIEnv* env, jobject obj, jstring data) {
    return env->NewStringUTF("Native Engine");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_qsupport_MainActivity_HTINative(JNIEnv* env, jobject obj, jstring data) {
    return env->NewStringUTF("Encryption");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_qsupport_MainActivity_NENative(JNIEnv* env, jobject obj, jstring data) {
    return env->NewStringUTF("Running (JNI/C++)");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_qsupport_MainActivity_ENNative(JNIEnv* env, jobject obj, jstring data) {
    return env->NewStringUTF("Hybrid AES/XOR-AF");
}
