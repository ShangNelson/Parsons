package com.parsons.bakery;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class CustomViewPager extends ViewPager {
    ViewPager pager;
    final Handler handler = new Handler();
    final Runnable runnable = new run();
    public CustomViewPager(@NonNull Context context) {
        super(context);
        pager = this;
    }

    public Handler getCustomHandler() {
        return handler;
    }

    public Runnable getCustomRunnable() {
        return runnable;
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        pager = this;
    }

    class run implements Runnable {

            @Override
            public void run() {
                setPageTransformer(false, (page, position) -> {
                    final float normalizedPosition = Math.abs(Math.abs(position) - 1);
                    page.setScaleX(normalizedPosition / 2 + 0.5f);
                    page.setScaleY(normalizedPosition / 2 + 0.5f);
                    page.setAlpha(normalizedPosition);
                    page.setRotationY(position * 80);
                });

                try {
                    Field mScroller;
                    mScroller = ViewPager.class.getDeclaredField("mScroller");
                    mScroller.setAccessible(true);
                    Scroller scroller = new Scroller(getContext(), new AccelerateInterpolator()) {
                        @Override
                        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                            super.startScroll(startX, startY, dx, dy, 1000);
                        }
                    };
                    mScroller.set(pager, scroller);
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (getCurrentItem() == getAdapter().getCount() - 1) {
                    setCurrentItem(0, true);
                } else {
                    setCurrentItem(getCurrentItem() + 1, true);
                }
                handler.postDelayed(this, 5000);
            }
        }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        try {
            int numChildren = getChildCount();
            for (int i = 0; i < numChildren; i++) {
                View child = getChildAt(i);
                if (child != null) {
                    child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    int h = child.getMeasuredHeight();
                    heightMeasureSpec = Math.max(heightMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}