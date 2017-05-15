//
// Created by yellowcat on 2017/5/15.
//

#ifndef CODECPLAYER_NATIVEUTIL_H
#define CODECPLAYER_NATIVEUTIL_H

#endif //CODECPLAYER_NATIVEUTIL_H
void copyDataY(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width,int height,
               int left,int top,int right,int bottom);
void copyDataUV(unsigned char* from, int len, unsigned char* to, int pixelStride, int rowStride, int width,int height,
                int left,int top,int right,int bottom);