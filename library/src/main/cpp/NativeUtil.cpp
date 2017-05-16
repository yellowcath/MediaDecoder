//
// Created by yellowcat on 2017/5/15.
//

#include <string.h>

void copyData(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width){
    if(pixelStride==1 && rowStride==width){
        memcpy(to,from,len);
        return;
    }
    int rowContentWidth = pixelStride * width;
    for(int i=0,toIndex=0;i<len;i+=pixelStride,toIndex++){
        if(i%rowStride<rowContentWidth){
            to[toIndex] = from[i];
        }
    }
}
