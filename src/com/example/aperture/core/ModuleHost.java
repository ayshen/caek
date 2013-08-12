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
    private ComponentName mComponent = null;
    private IModule mBinder = null;


    public ModuleHost(Context context) {
        mContext = new WeakReference<Context>(context);
    }


    public boolean bindTo(ComponentName name) {
        if(mBinder != null)
            unbind();

        mComponent = name;
        Intent bindIntent = new Intent(ModuleManagement.ACTION_BIND_MODULE);
        bindIntent.setComponent(name);

        return mContext.get().bindService(bindIntent, this,
                Context.BIND_AUTO_CREATE);
    }

    public void unbind() {
        mContext.get().unbindService(this);
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


    public ComponentName getComponent() {
        return mComponent;
    }

}
