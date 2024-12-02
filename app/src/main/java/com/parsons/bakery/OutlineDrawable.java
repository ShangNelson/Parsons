package com.parsons.bakery;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OutlineDrawable extends Drawable {
    private final int mStrokeWidth;
    private final int mColor;

    public OutlineDrawable(int strokeWidth, int color) {
        mStrokeWidth = strokeWidth;
        mColor = color;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect boundsOld = getBounds();
        RectF bounds = new RectF(boundsOld);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setColor(mColor);
        canvas.drawRoundRect(bounds, 10, 10, paint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
