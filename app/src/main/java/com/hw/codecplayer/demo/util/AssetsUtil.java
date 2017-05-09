package com.hw.codecplayer.demo.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by huangwei on 2017/5/9.
 */

public class AssetsUtil {
    public static void copyAssetsFileTo(Context context, String assetsFileName, File descFile) throws IOException {
        AssetManager am = context.getAssets();
        BufferedInputStream in = null;

        File parent = descFile.getParentFile();
        if (null != parent) {
            if (!FileUtils.checkFolder(parent)) {
                throw new IOException("Create Folder(" + parent.getAbsolutePath() + ") Failed!");
            }
        }

        try {
            in = new BufferedInputStream(am.open(assetsFileName, AssetManager.ACCESS_BUFFER));
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(descFile));

                byte[] buffer = new byte[8 * 1024];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            } finally {
                FileUtils.close(out);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            FileUtils.close(in);
        }
    }
}
