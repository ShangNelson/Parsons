package com.parsons.bakery.ui.order;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class ImageAnimator extends View {
    private final Bitmap image;
    private final Handler handler;
    public static final Object finished = new Object();
    private int x;
    private int y;
    private int targetX;
    private int targetY;
    private long startTime;
    private final long duration;

    public ImageAnimator(Context context, Bitmap image, int duration) {
        super(context);
        this.image = image;
        handler = new Handler(Looper.getMainLooper());
        this.duration = duration;
    }

    public void setTargetPosition(int x, int y) {
        targetX = x;
        targetY = y;
    }

    public void setStartingPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void startAnimation() {
        startTime = System.currentTimeMillis();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                double t = Math.min(1.0, (double) elapsedTime / duration);
                double easedT = t * t;

                int dx = (int) ((easedT * (targetX - x)) + x);
                int dy = (int) ((easedT * (targetY - y)) + y);

                x = dx;
                y = dy;

                if (x != targetX || y != targetY) {
                    handler.postDelayed(this, 10);
                } else {
                    synchronized (finished) {
                        finished.notifyAll();
                    }
                }

                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(image, x, y, new Paint());
    }
}



