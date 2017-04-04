package com.wulitao.hong.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * bitmap压缩工具类
 */
public class BitmapUtil {

    public static Bitmap getProperBitmap(File file){
        return getProperBitmap(file, 0, 0);
    }

    public static Bitmap getProperBitmap(File file, int reqWidth, int reqHeight){
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return  getProperBitmap(is, reqWidth, reqHeight);
    }

    public static Bitmap getProperBitmap(InputStream is){
        return  getProperBitmap(HttpUtil.getBytes(is), 0, 0);
    }

    public static Bitmap getProperBitmap(InputStream is, int reqWidth, int reqHeight){
        return  getProperBitmap(HttpUtil.getBytes(is), reqWidth, reqHeight);
    }

    public static Bitmap getProperBitmap(byte[] bytes) {
        return getProperBitmap(bytes, 0, 0);
    }

    /**
     * 直接decodeStream，第二次会返回null，因为is已经改变，所以采用decodeByteArray的方式
     */
    public static Bitmap getProperBitmap(byte[] bytes, int reqWidth, int reqHeight){
        if (bytes == null){
            return  null;
        }
        if (reqWidth == 0 && reqHeight == 0){
            // 不进行压缩
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 设置只解析宽高，防止OOM
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        // 计算缩放的比例（double）
        double realWidth = options.outWidth;
        double realHeight = options.outHeight;
        double widthRate = realWidth / reqWidth;
        double heightRate = realHeight / reqHeight;
        double rate = widthRate > heightRate ? widthRate : heightRate;
        // 重新解析图片，得到合适的bitmap
        int inSampleSize = (int) Math.ceil(rate);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return  bitmap;
    }
}