package com.parsons.bakery;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class Firebase extends FirebaseMessagingService {
    public static final String TAG = "TOKEN";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG,s);
        System.out.println(s);
    }
}
