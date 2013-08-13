package com.example.aperture.core;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.ref.WeakReference;


/** Wrapper class for dealing with the service connections underlying the
module system.
Handles connecting to and disconnecting from the module services.
*/
public class ModuleHost implements ServiceConnection {

    /** A weak reference to the parent Context, to allow it to be gc'd
    regardless of whether these host objects leak. */
    private WeakReference<Context> mContext;

    /** The module currently connected to this host. */
    private ComponentName mComponent = null;

    /** The communication interface provided by the currently connected
    module. */
    private IModule mBinder = null;


    /** Create a host for a Context.
    @param context the hosting Context.
    */
    public ModuleHost(Context context) {
        mContext = new WeakReference<Context>(context);
    }


    /** Connect to a module.
    Disassociate from any previously connected module, then bind to the
    specified module service.
    @param name the component of the module to which to connect.
    @return whether the bind operation succeeded.
    */
    public boolean bindTo(ComponentName name) {
        if(mBinder != null)
            unbind();

        mComponent = name;
        Intent bindIntent = new Intent(ModuleManagement.ACTION_BIND_MODULE);
        bindIntent.setComponent(name);

        return mContext.get().bindService(bindIntent, this,
                Context.BIND_AUTO_CREATE);
    }


    /** Disconnect from a module. */
    public void unbind() {
        mContext.get().unbindService(this);
    }


    /** Handle a connection to a module being established successfully.
    Actual communication with the module is handled in the parent Context.
    @param name the component representing the module.
    @param service the communication interface to the module.
    */
    public void onServiceConnected(ComponentName name, IBinder service) {
        if(!name.equals(mComponent)) return;
        mBinder = IModule.Stub.asInterface(service);
    }


    /** Handle disconnecting from a module.
    @param name the component representing the module.
    */
    public void onServiceDisconnected(ComponentName name) {
        if(!name.equals(mComponent)) return;
        mBinder = null;
    }


    /** Get the currently bound module service's communication interface. */
    public IModule getBinder() {
        return mBinder;
    }


    /** Get the currently bound module's component. */
    public ComponentName getComponent() {
        return mComponent;
    }

}
