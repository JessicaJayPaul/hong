package com.wulitao.hong.utils;

import android.content.Context;

/**
 * Created by cjt-pc on 2015/8/17.
 * Email:879309896@qq.com
 */
public class DensityUtil {

    public static int dpTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int pxTodip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
