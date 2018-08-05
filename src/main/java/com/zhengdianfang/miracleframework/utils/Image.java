package com.zhengdianfang.miracleframework.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class Image {
    public static Bitmap loadBitmapFromView(View v, int width , int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, width, height);
        v.draw(c);
        return b;
    }
}
