package com.wulitao.hong.utils;

import android.content.Context;

import com.wulitao.hong.io.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 磁盘缓存工具类
 */

public class DiskLruCacheUtil {

    private static final int VALUE_COUNT = 1;

    private static final long CACHE_SIZE = 20 * 1024 * 1024;

    private static final String DIRECTORY_NAME = "bitmap";

    private DiskLruCache diskLruCache;

    public DiskLruCacheUtil(Context context){
        File directory = SystemUtil.getDiskCacheDir(context, DIRECTORY_NAME);
        int version = SystemUtil.getAppVersion(context);
        try {
            diskLruCache = DiskLruCache.open(directory, version, VALUE_COUNT, CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过url从磁盘缓存中获取InputStream
     */
    public InputStream getInputStreamFromDiskLruCacheByUrl(String url){
        InputStream is = null;
        if (diskLruCache != null){
            String key = EncryptUtil.encryptByMd5(url);
            try {
                DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
                if (snapShot != null){
                    is = snapShot.getInputStream(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return is;
    }

    public void addInputStreamToDiskLruCache(String url, InputStream is){
        if (diskLruCache == null){
            return;
        }
        try {
            String key = EncryptUtil.encryptByMd5(url);
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            OutputStream os = editor.newOutputStream(0);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1){
                os.write(buffer, 0, len);
            }
            editor.commit();
            diskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
