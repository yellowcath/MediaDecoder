#include <jni.h>
#include <string>
#include "NativeUtil.h"

#ifndef _Included_com_hw_mediadecoder_util_NativeUtil
#define _Included_com_hw_mediadecoder_util_NativeUtil
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_hw_mediadecoder_util_NativeUtil_planesToYUV420p(JNIEnv *env, jclass type, jobject buffer1, jobject buffer2,
                                                         jobject buffer3, jint capacity1, jint capacity2,
                                                         jint capacity3,
                                                         jint pixelStride1, jint pixelStride2, jint pixelStride3,
                                                         jint rowStride1, jint rowStride2, jint rowStride3,
                                                         jint width, jint height,
                                                         jobject bufferY, jobject bufferU,
                                                         jobject bufferV) {
    int pStride1 = pixelStride1;
    int pStride2 = pixelStride2;
    int pStride3 = pixelStride3;
    int len1 = capacity1;
    int len2 = capacity2;
    int len3 = capacity3;
    int rStride1 = rowStride1;
    int rStride2 = rowStride2;
    int rStride3 = rowStride3;

    int w = width;

    unsigned char* data1 = (unsigned char *) env->GetDirectBufferAddress(buffer1);
    unsigned char* data2 = (unsigned char *) env->GetDirectBufferAddress(buffer2);
    unsigned char* data3 = (unsigned char *) env->GetDirectBufferAddress(buffer3);

    unsigned char* y = (unsigned char *) env->GetDirectBufferAddress(bufferY);
    unsigned char* u = (unsigned char *) env->GetDirectBufferAddress(bufferU);
    unsigned char* v =(unsigned char *)  env->GetDirectBufferAddress(bufferV);

    int uvWidth = w/2;
    copyData(data1,len1,y,pStride1,rStride1,w);
    copyData(data2,len2,u,pStride2,rStride2,uvWidth);
    copyData(data3,len3,v,pStride3,rStride3,uvWidth);
}


JNIEXPORT void JNICALL
Java_com_hw_mediadecoder_util_NativeUtil_planesToYUV(JNIEnv *env, jclass type,
                                                     jobject buffer1, jobject buffer2, jobject buffer3,
                                                     jint capacity1, jint capacity2, jint capacity3,
                                                     jint pixelStride1, jint pixelStride2, jint pixelStride3,
                                                     jint rowStride1, jint rowStride2, jint rowStride3,
                                                     jint width, jint height,
                                                     jobject bufferYUV) {

    int pStride1 = pixelStride1;
    int pStride2 = pixelStride2;
    int pStride3 = pixelStride3;
    int len1 = capacity1;
    int len2 = capacity2;
    int len3 = capacity3;
    int rStride1 = rowStride1;
    int rStride2 = rowStride2;
    int rStride3 = rowStride3;

    int w = width;
    int uvWidth = w / 2;

    unsigned char *data1 = (unsigned char *) env->GetDirectBufferAddress(buffer1);
    unsigned char *data2 = (unsigned char *) env->GetDirectBufferAddress(buffer2);
    unsigned char *data3 = (unsigned char *) env->GetDirectBufferAddress(buffer3);

    unsigned char *y = (unsigned char *) env->GetDirectBufferAddress(bufferYUV);
    unsigned char *u = y + (width * height);
    unsigned char *v = u + (width * height / 4);

    copyData(data1, len1, y, pStride1, rStride1, w);
    copyData(data2, len2, u, pStride2, rStride2, uvWidth);
    copyData(data3, len3, v, pStride3, rStride3, uvWidth);
}

JNIEXPORT void JNICALL
Java_com_hw_mediadecoder_util_NativeUtil_planesToYUV420sp(JNIEnv *env, jclass type, jobject buffer1, jobject buffer2,
                                                          jobject buffer3, jint capacity1, jint capacity2,
                                                          jint capacity3,
                                                          jint pixelStride1, jint pixelStride2, jint pixelStride3,
                                                          jint rowStride1, jint rowStride2, jint rowStride3,
                                                          jint width, jint height,
                                                          jobject bufferY, jobject bufferUV) {

    int pStride1 = pixelStride1;
    int pStride2 = pixelStride2;
    int pStride3 = pixelStride3;
    int len1 = capacity1;
    int len2 = capacity2;
    int len3 = capacity3;
    int rStride1 = rowStride1;
    int rStride2 = rowStride2;
    int rStride3 = rowStride3;

    int w = width;
    int uvWidth = w / 2;

    unsigned char *data1 = (unsigned char *) env->GetDirectBufferAddress(buffer1);
    unsigned char *data2 = (unsigned char *) env->GetDirectBufferAddress(buffer2);
    unsigned char *data3 = (unsigned char *) env->GetDirectBufferAddress(buffer3);

    unsigned char *y = (unsigned char *) env->GetDirectBufferAddress(bufferY);
    unsigned char *u = (unsigned char *) env->GetDirectBufferAddress(bufferUV);
    unsigned char *v = u + (width * height / 4);

    copyData(data1, len1, y, pStride1, rStride1, w);
    copyData(data2, len2, u, pStride2, rStride2, uvWidth);
    copyData(data3, len3, v, pStride3, rStride3, uvWidth);
}
#ifdef __cplusplus
}
#endif
#endif
