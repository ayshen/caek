package com.example.aperture.core;

import android.content.Intent;


/** Wrapper class for an Intent that resulted from querying a module.
Currently used to make android.R.layout.simple_list_item_1 look better with the
ArrayAdapter. This may not be necessary in the future.
*/
public class IntentWrapper {

    public final Intent mIntent;

    public IntentWrapper(Intent intent) { mIntent = intent; }

    public String toString() {
        String s = mIntent.getStringExtra(Module.RESPONSE_TEXT);
        if(s == null) return mIntent.toString();
        else return s;
    }
}
