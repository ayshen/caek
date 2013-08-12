package com.example.aperture.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public abstract class Module extends Service {


    public final static String QUERY_TEXT =
            "com.example.aperture.core.Module.QUERY_TEXT";
    public final static String RESPONSE_TEXT =
            "com.example.aperture.core.Moduel.RESPONSE_TEXT";
    public final static String RESPONSE_THUMBNAIL =
            "com.example.aperture.core.Moduel.RESPONSE_THUMBNAIL";


    protected IBinder mBinder = null;


    public IBinder onBind(Intent intent) {
        if(intent.getAction().equals(ModuleManagement.ACTION_BIND_MODULE)) {
            if(mBinder == null) {
                mBinder = createBinder();
            }
            return mBinder;
        }
        else return null;
    }


    protected abstract IBinder createBinder();

}
