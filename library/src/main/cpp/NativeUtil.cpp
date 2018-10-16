//
// Created by yellowcat on 2017/5/15.
//

#include <string>
#include <stdio.h>
#include "NativeUtil.h"

void copyData(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width){
    if(pixelStride==1 && rowStride==width){
        memcpy(to,from,len);
        return;
    }
    int rowContentWidth = pixelStride * width;
    LOGE("p:%d,r:%d,width:%d,rowContentWidth:%d\n",pixelStride,rowStride,width,rowContentWidth);
    for(int i=0,toIndex=0;i<len;){
        if(i%rowStride<rowContentWidth){
            to[toIndex++] = from[i++];
        }else{
            i++;
        }
    }

    for(int i=0;i<len/2;i++){
        LOGE("%d ",to[i]);
    }
}
