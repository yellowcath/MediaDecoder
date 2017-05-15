//
// Created by yellowcat on 2017/5/15.
//

#include <string.h>

void copyDataY(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width,int height,
int left,int top,int right,int bottom){
    int rectWidth = right-left;
    int rectHeight = bottom-top;
    if(pixelStride==1 && rowStride==width && width == rectWidth && height ==rectHeight){
        memcpy(to,from,len);
        return;
    }
    int rowContentWidth = pixelStride * width;
    for(int i=0,toIndex=0;i<len;i+=pixelStride,toIndex++){
        int columnIndex = i%rowStride;
        int rowIndex = i/rowStride;
        if(columnIndex<rowContentWidth && columnIndex>=left && columnIndex<=right && rowIndex>=top && rowIndex<=bottom){
            to[toIndex] = from[i];
        }
    }
}

void copyDataUV(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width,int height,
               int left,int top,int right,int bottom){
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