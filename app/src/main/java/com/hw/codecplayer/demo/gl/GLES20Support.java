package com.hw.codecplayer.demo.gl;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;


public class GLES20Support {

    public static boolean detectOpenGLES20(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

//    public static Dialog getNoSupportGLES20Dialog(final Activity activity) {
//        AlertDialog.Builder b = new AlertDialog.Builder(activity);
//        b.setCancelable(false);
//        b.setTitle(R.string.gl_no_support_title);
//        b.setMessage(R.string.gl_no_support_message);
//        b.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                activity.finish();
//            }
//        });
//        return b.create();
//    }
}
