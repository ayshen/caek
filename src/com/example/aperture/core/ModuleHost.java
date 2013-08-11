package com.example.aperture.core;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.ref.WeakReference;


public class ModuleHost implements ServiceConnection {

    private WeakReference<Context> mContext;
    private ComponentName mComponent;
    private IModule.Stub mBinder;


    public ModuleHost(Context context) {
        mContext = new WeakReference<Context>(context);
    }


    public void bindTo(ComponentName name) {
        if(mBinder != null)
            mContext.get().unbindService(this);

        mComponent = name;
        Intent bindIntent = new Intent(ModuleManagement.ACTION_BIND_MODULE);

        if(!mContext.get().bindService(bindIntent, this,
                Context.BIND_AUTO_CREATE)) {
            android.util.Log.w(this.toString(), "can't bind to " + name);
        }
    }


    public void onServiceConnected(ComponentName name, IBinder service) {
        if(!name.equals(mComponent)) return;
        mBinder = IModule.Stub.asInterface(service);
    }


    public void onServiceDisconnected(ComponentName name) {
        if(!name.equals(mComponent)) return;
        mBinder = null;
    }


    public IModule getBinder() {
        return mBinder;
    }

}
