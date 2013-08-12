package com.example.aperture.core;

import android.content.Intent;


public class IntentWrapper {

    public final Intent mIntent;

    public IntentWrapper(Intent intent) { mIntent = intent; }

    public String toString() {
        String s = mIntent.getStringExtra(Module.RESPONSE_TEXT);
        if(s == null) return mIntent.toString();
        else return s;
    }
}
