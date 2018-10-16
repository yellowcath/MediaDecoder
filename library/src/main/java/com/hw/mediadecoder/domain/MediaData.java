package com.hw.mediadecoder.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangwei on 2017/5/9.
 */

public class MediaData implements Parcelable {
    public static final int END_TIME_VIDEO_END = -1;
    public String mediaPath;
    public long startTimeMs;
    public long endTimeMs;
    /**
     * 是否按指定时间段截取数据
     */
    public boolean shouldCut;

    public MediaData(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public MediaData(String mediaPath, long startTimeMs, long endTimeMs) {
        this.mediaPath = mediaPath;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        shouldCut = true;
    }

    @Override
    public String toString() {
        return "MediaData{" +
                "mediaPath='" + mediaPath + '\'' +
                ", startTimeMs=" + startTimeMs +
                ", endTimeMs=" + endTimeMs +
                ", shouldCut=" + shouldCut +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mediaPath);
        dest.writeLong(this.startTimeMs);
        dest.writeLong(this.endTimeMs);
        dest.writeByte(this.shouldCut ? (byte) 1 : (byte) 0);
    }

    protected MediaData(Parcel in) {
        this.mediaPath = in.readString();
        this.startTimeMs = in.readLong();
        this.endTimeMs = in.readLong();
        this.shouldCut = in.readByte() != 0;
    }

    public static final Parcelable.Creator<MediaData> CREATOR = new Parcelable.Creator<MediaData>() {
        @Override
        public MediaData createFromParcel(Parcel source) {
            return new MediaData(source);
        }

        @Override
        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };
}
