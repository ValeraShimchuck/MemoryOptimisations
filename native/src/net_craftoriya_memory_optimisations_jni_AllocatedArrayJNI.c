#include "net_craftoriya_memory_optimisations_jni_AllocatedArrayJNI.h"
#include <jemalloc/jemalloc.h>
#include <string.h>


JNIEXPORT jlong JNICALL Java_net_craftoriya_memory_1optimisations_jni_AllocatedArrayJNI_allocate
(JNIEnv *env, jclass jobj, jint size) {
    jlong ptr = (jlong) malloc(size);
    memset((void *) ptr, 0, size); // TODO remove
    return ptr;
}

JNIEXPORT void JNICALL Java_net_craftoriya_memory_1optimisations_jni_AllocatedArrayJNI_deallocate
(JNIEnv *env, jclass jobj, jlong ptr) {
    free((void *) ptr);
}

JNIEXPORT void JNICALL Java_net_craftoriya_memory_1optimisations_jni_AllocatedArrayJNI_write
  (JNIEnv *env, jclass class, jlong ptr, jint offset, jbyteArray bytes) {
    jbyte *b = (*env)->GetByteArrayElements(env, bytes, NULL);
    memcpy((void *) ((char *) ptr + offset), b, (*env)->GetArrayLength(env, bytes));
    (*env)->ReleaseByteArrayElements(env, bytes, b, 0);
  }


JNIEXPORT void JNICALL Java_net_craftoriya_memory_1optimisations_jni_AllocatedArrayJNI_read
  (JNIEnv *env, jclass class, jlong ptr, jint offset, jbyteArray bytes) {
    jbyte *b = (*env)->GetByteArrayElements(env, bytes, NULL);
    memcpy(b, (void *) ((char *) ptr + offset), (*env)->GetArrayLength(env, bytes));
    (*env)->ReleaseByteArrayElements(env, bytes, b, 0);
  }


JNIEXPORT void JNICALL Java_net_craftoriya_memory_1optimisations_jni_AllocatedArrayJNI_copy
  (JNIEnv *env, jclass class, jlong dstPtr, jint dstOffset, jlong srcPtr, jint srcOffset, jint length) {
    memcpy((void *) ((char *) dstPtr + dstOffset), (void *) ((char *) srcPtr + srcOffset), length);
  }