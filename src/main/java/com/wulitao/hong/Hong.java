package com.wulitao.hong;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.wulitao.hong.utils.BitmapLruCacheUtil;
import com.wulitao.hong.utils.BitmapUtil;
import com.wulitao.hong.utils.DiskLruCacheUtil;
import com.wulitao.hong.utils.HttpUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class Hong {

    private static Hong hong = null;

    private Resources resources;

    private DiskLruCacheUtil diskLruCacheUtil;

    private BitmapLruCacheUtil bitmapLruCacheUtil;

    private Object imgRes;

    private int width;

    private int height;

    private Hong(){}

    /**
     * 单例设计模式，懒汉式加载，同时初始化内部资源（双重锁）
     */
    public static Hong with(Context context){
        if (hong == null){
            synchronized (Hong.class){
                if (hong == null){
                    hong = new Hong();
                    hong.resources = context.getResources();
                    hong.diskLruCacheUtil = new DiskLruCacheUtil(context);
                    hong.bitmapLruCacheUtil = BitmapLruCacheUtil.getInstance();
                }
            }
        }
        return hong;
    }

    public Hong load(String path){
        this.imgRes = path;
        return this;
    }

    public Hong load(int id){
        this.imgRes = id;
        return this;
    }

    public Hong resize(int width, int height){
        this.width = width;
        this.height = height;
        return this;
    }

    public void into(ImageView imageView){
        Bitmap bitmap;
        // 去项目资源取
        if (imgRes instanceof Integer){
            InputStream inputStream = resources.openRawResource(Integer.parseInt(imgRes.toString()));
            bitmap = BitmapUtil.getProperBitmap(inputStream, width, height);
            imageView.setImageBitmap(bitmap);
            return;
        }

        String path = imgRes.toString();
        File file = new File(path);
        // 去本地文件取
        if (!file.exists()){
            // 去内存缓存取
            bitmap = bitmapLruCacheUtil.getBitmapFromLruCache(path);
            if (bitmap == null){
                // 去磁盘缓存取
                InputStream is = diskLruCacheUtil.getInputStreamFromDiskLruCacheByUrl(path);
                if (is == null){
                    // 发起http请求，获取网络资源（同时添加内存缓存、磁盘缓存中）
                    new ImageCacheTask(path, imageView).execute(width, height);
                } else {
                    bitmap = BitmapUtil.getProperBitmap(is, width, height);
                    // 磁盘缓存已存在，添加到内存缓存中
                    bitmapLruCacheUtil.addBitmapToLruCache(path, bitmap);
                }
            }
        } else{
            bitmap = BitmapUtil.getProperBitmap(file, width, height);
        }
        imageView.setImageBitmap(bitmap);

        // 初始化全局属性
        imgRes = "";
        width = 0;
        height = 0;
    }

    /**
     * 加载网络图片资源任务类
     */
    private class ImageCacheTask extends AsyncTask<Integer, Void, Bitmap> {

        private String url;

        private ImageView imageView;

        private ImageCacheTask(String url, ImageView imageView){
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int width = params[0];
            int height = params[1];
            Object response = HttpUtil.getInstance().connect(url)
                    .byteArray()
                    .get();
            if (response instanceof Exception){
                // 获取网络资源异常
                return null;
            }
            byte[] bytes = (byte[]) response;
            Bitmap bitmap = BitmapUtil.getProperBitmap(bytes, width, height);
            // 写入内存缓存
            bitmapLruCacheUtil.addBitmapToLruCache(url, bitmap);
            // 写入磁盘缓存
            InputStream is = new ByteArrayInputStream(bytes);
            diskLruCacheUtil.addInputStreamToDiskLruCache(url, is);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // 主线程更新ImageView
            imageView.setImageBitmap(bitmap);
        }
    }
}
