package com.parsons.bakery;

public class backgroundWork {
    private static boolean boo = false;
    private static ChangeListener listener;

    public static boolean isBoo() {
        return boo;
    }

    public static void setBoo(boolean seter) {
        boo = boo;
        if (listener != null) listener.onChange();
    }

    public static ChangeListener getListener() {
        return listener;
    }

    public static void setListener(ChangeListener setListener) {
        listener = setListener;
    }

    public interface ChangeListener {
        void onChange();
    }
}