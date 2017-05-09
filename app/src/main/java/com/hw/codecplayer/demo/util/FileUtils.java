/*
 *                                                                                        
 * Copyright (c)2010-2012  Pinguo Company
 *                 品果科技                            版权所有 2010-2012
 * 
 * PROPRIETARY RIGHTS of Pinguo Company are involved in the
 * subject matter of this material.  All manufacturing, reproduction, use,
 * and sales rights pertaining to this subject matter are governed by the
 * license agreement.  The recipient of this software implicitly accepts   
 * the terms of the license.
 * 本软件文档资料是品果公司的资产,任何人士阅读和使用本资料必须获得
 * 相应的书面授权,承担保密责任和接受相应的法律约束.
 * 
 * FileName:FileUtils.java
 * Author:liubo
 * Date:Dec 27, 2012 10:38:55 AM 
 * 
 */

package com.hw.codecplayer.demo.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件操作工具类
 *
 * @author liubo
 * @version 4.0
 * @since 4.0
 */
public final class FileUtils {
    public static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String TAG = FileUtils.class.getSimpleName();
    private static final int BUFFER_SIZE = 8 * 1024;
    public static final String BLOCK_BUSTER = "blockbuster";

    private FileUtils() {

    }

    public static String getUninstallFolder(Context context, String parent) {
        String uninstallFolder = context.getFilesDir() + File.separator + parent + File.separator + "uninstall_" + parent + File.separator;
        checkFolder(uninstallFolder);
        return uninstallFolder;
    }

    public static String getInstallFolder(Context context, String parent) {
        String uninstallFolder = context.getFilesDir() + File.separator + parent + File.separator + "install_" + parent + File.separator;
        checkFolder(uninstallFolder);
        return uninstallFolder;
    }

    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                if (!destFile.delete()) {
                    Log.w(TAG, "Delete file failed!");
                }
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 拷贝单个文件
     *
     * @param srcPath  源文件
     * @param destPath 目标文件
     * @author liubo
     */
    public static void copySingleFile(String srcPath, String destPath) throws IOException {
        if (srcPath == null || destPath == null) {
            throw new IOException("path is Null, srcPath=" + srcPath + ",destPath=" + destPath);
        }
        copySingleFile(new File(srcPath), new File(destPath));
    }

    /**
     * 拷贝单个文件（不管目标文件是否存在，都会创建一个空的文件）
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @author liubo
     */
    public static void copySingleFile(File srcFile, File destFile) throws IOException {
        File parent = destFile.getParentFile();
        if (!checkFolder(parent)) {
            throw new IOException("Create Folder(" + parent.getAbsolutePath() + ") Failed!");
        }

        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(srcFile));
            BufferedOutputStream out = null;
            try {
                // 不管目标文件是否存在，都会创建一个空的文件！
                out = new BufferedOutputStream(new FileOutputStream(destFile));
                byte[] buffer = new byte[BUFFER_SIZE];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            } finally {
                close(out);
            }
        } finally {
            close(in);
        }
    }

    public static void copyFolder(File src, File dest) throws IOException {
        Log.d(TAG, "Copy from: " + src.getAbsolutePath() + " to: " + dest.getAbsolutePath());
        if (src.isDirectory()) {
            checkFolder(dest);

            String files[] = src.list();

            if (null == files || files.length == 0) {
                Log.d(TAG, "files is empty and can't do copy");
                return;
            }

            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                // 递归
                copyFolder(srcFile, destFile);
            }

        } else {
            Log.d(TAG, "Copy file from: " + src.getAbsolutePath() + " to: " + dest.getAbsolutePath());
            copySingleFile(src, dest);
        }
    }

    /**
     * 将文件转换为byte[] 数据.
     *
     * @param filePath 文件路径
     * @return byte[] 文件数据
     * @throws IOException
     * @author zengchuanmeng
     */
    public static byte[] getFileData(String filePath) throws IOException {
        return getFileData(new File(filePath));
    }

    /**
     * 将文件转换为byte[] 数据.
     *
     * @param file 文件
     * @return byte[] 文件数据
     * @throws IOException
     * @author zengchuanmeng
     */
    public static byte[] getFileData(File file) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            return getStreamData(in);
        } finally {
            close(in);
        }
    }

    /**
     * 从系统data/应用包目录下 获取文件数据.
     *
     * @param fileName
     * @return byte[]
     * @author liubo
     */
    public static byte[] getPkgFileData(Context context, String fileName) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(context.openFileInput(fileName));
            return getStreamData(in);
        } finally {
            close(in);
        }
    }

    /**
     * 文件夹检查，不存在则新建
     *
     * @param folderPath 文件夹检查，不存在则新建
     * @return true，存在或新建成功，false，不存在或新建失败
     * @author liubo
     */
    public static boolean checkFolder(String folderPath) {
        if (folderPath == null) {
            return false;
        }
        return checkFolder(new File(folderPath));
    }

    /**
     * 文件夹检查，不存在则新建
     *
     * @param folder 文件夹检查，不存在则新建
     * @return true，存在或新建成功，false，不存在或新建失败
     * @author liubo
     */
    public static boolean checkFolder(File folder) {
        if (folder == null) {
            return false;
        }

        if (folder.isDirectory()) {
            return true;
        }

        return folder.mkdirs();
    }

    /**
     * 取得文件内容
     *
     * @param file 文件
     * @return 文件
     * @throws Exception 读取异常
     * @author liubo
     */
    public static String getFileContent(File file) throws IOException {
        BufferedReader in = null;
        long fileSize = file.length();
        if (fileSize > Short.MAX_VALUE) {
            fileSize = Short.MAX_VALUE;
        }
        StringBuilder sb = new StringBuilder((int) fileSize);
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            sb.deleteCharAt(sb.length() - 1);
        } finally {
            if (in != null) {
                in.close();
                in = null;
            }
        }

        return sb.toString();
    }

    /**
     * 将字符串写入文件
     *
     * @param file    写入的文件
     * @param content 文件内容
     * @throws Exception 异常
     * @author liubo
     */
    public static void writeFileContent(File file, String content) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(content.getBytes("utf-8"));
            out.flush();
        } finally {
            close(out);
        }
    }

    public static void deleteFile(String path) {
        if (null == path || "".equals(path)) {
            Log.e(TAG, "File path is null or not exist, delete file fail!");
            return;
        }

        File file = new File(path);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        if (null == file || !file.exists()) {
            Log.e(TAG, "File is null or not exist, delete file fail!");
            return;
        }

        if (file.isDirectory()) {
            deleteFile(file.listFiles());
        }

        if (!file.delete()) {
            Log.i(TAG, "delete (" + file.getPath() + ") failed!");
        }
    }

    public static void deleteFile(File[] files) {
        if (null == files || files.length == 0) {
            Log.e(TAG, "Files is null or empty, delete fail!");
            return;
        }

        for (File file : files) {
            deleteFile(file);
        }
    }


    public static void close(InputStream in) throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
    }

    public static void close(OutputStream out) throws IOException {
        if (out != null) {
            out.close();
            out = null;
        }
    }

    public static byte[] getStreamData(InputStream in) throws IOException {
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            return out.toByteArray();
        } finally {
            close(out);
        }
    }

    /**
     * 该方法用于检查.nomedia文件，没有则新建
     *
     * @param
     * @return void
     * @throws IOException
     * @author tangzhen
     */
    public static boolean checkNomediaFile(String path) throws IOException {
        File nomedia = new File(path + File.separator + ".nomedia");

        if (!nomedia.exists()) {
            return nomedia.createNewFile();
        }
        return true;
    }

    /**
     * 保存图像，该方法对为防止其他程序读取未保存完的图像做了特殊处理
     *
     * @param imgData 图像资源
     * @param path    存储路径
     * @throws IOException IO异常
     * @author liubo
     */
    public static boolean saveJpeg(byte[] imgData, String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }

        String tmpPath = path + ".tmp";
        saveFile(imgData, tmpPath);
        return new File(tmpPath).renameTo(new File(path));
    }

    /**
     * 保存Bitmap
     *
     * @param
     * @return boolean
     * @author litao
     */
    public static boolean saveBitmap(String path, Bitmap bitmap, int quality) throws IOException, IllegalArgumentException {
        if (TextUtils.isEmpty(path) || bitmap == null)
            return false;

        boolean flag = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();

                flag = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(out);
        }

        return flag;
    }

    /**
     * 保存文件
     *
     * @param data 二进制数据文件
     * @param path 路径
     * @throws IOException IOException
     * @author liubo
     */
    public static void saveFile(byte[] data, String path) throws IOException {
        if (data == null) {
            throw new IOException("data is null");
        }

        if (path == null) {
            throw new IOException("path is null");
        }

        File parent = new File(path).getParentFile();
        if (!checkFolder(parent)) {
            throw new IOException("Create Folder(" + parent.getAbsolutePath() + ") Failed!");
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(path));
            out.write(data);
        } finally {
            close(out);
        }
    }

    /**
     * 返回文本文件行数
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static int getLineNumber(File file) {
        LineNumberReader lnr = null;
        int count = 0;

        try {

            lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = lnr.readLine()) != null) {
                count += 1;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != lnr) {
                    lnr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return count;
        }
    }

    /**
     * @param context
     * @param file
     * @deprecated 因为发送广播是个异步过程，有的手机上处理太慢，导致拍照后会在系统相册里面出现两张照片
     */
    public static void fileScan(Context context, String file) {
        Uri data = Uri.parse("file://" + file);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

    /**
     * Copy from one stream to another.  Throws IOException in the event of error
     * (for example, SD card is full)
     *
     * @param is Input stream.
     * @param os Output stream.
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        copyStream(is, os, buffer, BUFFER_SIZE);
    }

    /**
     * Copy from one stream to another.  Throws IOException in the event of error
     * (for example, SD card is full)
     *
     * @param is         Input stream.
     * @param os         Output stream.
     * @param buffer     Temporary buffer to use for copy.
     * @param bufferSize Size of temporary buffer, in bytes.
     */
    public static void copyStream(InputStream is, OutputStream os,
                                  byte[] buffer, int bufferSize) throws IOException {
        try {
            for (; ; ) {
                int count = is.read(buffer, 0, bufferSize);
                if (count == -1) {
                    break;
                }
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    public static Bitmap.CompressFormat getPhotoType(String path) {
        if (path != null) {
            Bitmap.CompressFormat format = getPhotoTypeForHead(path);
            if (format != null) {
                return format;

            } else {
                format = getPhotoTypeForPostfix(path);

                if (format != null) {
                    return format;

                } else {
                    return Bitmap.CompressFormat.JPEG;

                }
            }
        }

        return null;
    }

    public static Bitmap.CompressFormat getPhotoTypeForHead(String path) {
        FileInputStream fileInputStream = null;
        byte[] array = new byte[2];
        try {
            fileInputStream = new FileInputStream(path);
            fileInputStream.read(array, 0, array.length);
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        String str = bytesToHexString(array);
        if ("FFD8".equals(str)) {
            return Bitmap.CompressFormat.JPEG;
        } else if ("8950".equals(str)) {
            return Bitmap.CompressFormat.PNG;
        } else {
            return null;
        }

    }

    public static Bitmap.CompressFormat getPhotoTypeForPostfix(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("jpg") || lowerPath.endsWith("jpeg")) {
            return Bitmap.CompressFormat.JPEG;
        } else if (lowerPath.endsWith("png")) {
            return Bitmap.CompressFormat.PNG;
        } else {
            return null;
        }
    }

    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }


    public static void unzip(String zipFile, String location) {
        byte[] _buffer = new byte[8192];
        FileInputStream fin = null;
        ZipInputStream zin = null;
        BufferedOutputStream fout = null;
        File tmp = null;

        try {
            fin = new FileInputStream(zipFile);
            zin = new ZipInputStream(fin);
            ZipEntry e = null;

            while ((e = zin.getNextEntry()) != null) {
                if (e.isDirectory()) {
                    dirChecker(location, e.getName());
                } else {
                    String subPath = e.getName();

                    if (subPath.contains("/")) {
                        int endIndex = subPath.lastIndexOf("/");
                        dirChecker(location, subPath.substring(0, endIndex));
                    }

                    tmp = new File(location + e.getName());
                    fout = new BufferedOutputStream(new FileOutputStream(tmp));
                    FileUtils.copyStream(zin, fout, _buffer, 8192);
                    zin.closeEntry();
                    fout.close();
                    fout = null;

                    tmp = null;
                }
            }

            zin.close();
            zin = null;
        } catch (IOException var25) {
            throw new RuntimeException(var25);
        } finally {
            if (tmp != null) {
                try {
                    tmp.delete();
                } catch (Exception var24) {
                    var24.printStackTrace();
                }
            }

            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception var23) {
                }
            }

            if (zin != null) {
                try {
                    zin.closeEntry();
                } catch (Exception var22) {
                }
            }

            if (fin != null) {
                try {
                    fin.close();
                } catch (Exception var21) {
                }
            }
            try {
                File file = new File(zipFile);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void dirChecker(String location, String dir) {
        File f = new File(location + dir);
        if (!f.isDirectory() && !f.mkdirs()) {
//            L.e(TAG, "Create dir failed!");
        }
    }
}
