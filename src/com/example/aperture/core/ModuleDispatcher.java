package com.example.aperture.core;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;


public class ModuleDispatcher {

    private WeakReference<Context> mContext = null;
    private List<ComponentName> mModules = null;
    private List<ModuleHost> mHosts = null;


    public ModuleDispatcher(Context context) {
        mContext = new WeakReference<Context>(context);
        loadModuleListFromPreferences();
        initializeModuleHosts();
    }


    /** Load the list of enabled modules from shared preferences.
    */
    private void loadModuleListFromPreferences() {
        if(mContext == null || mContext.get() == null) {
            throw new NullPointerException("parent Context for " +
                    this + " no longer exists");
        }

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(
                mContext.get());

        Set<String> ns = prefs.getStringSet(
                ModuleManagement.LAST_ALL_MODULES,
                new TreeSet<String>());

        mModules = new ArrayList<ComponentName>();

        for(String n: ns) {
            if(prefs.getBoolean(n, false)) {
                mModules.add(ComponentName.unflattenFromString(n));
            }
        }
    }


    /** Set up hosting for enabled modules.
    */
    private void initializeModuleHosts() {
        if(mContext == null || mContext.get() == null) {
            throw new NullPointerException("parent Context for " +
                    this + " no longer exists");
        }

        if(mModules == null) {
            loadModuleListFromPreferences();
        }

        if(mHosts != null) {
            destroy();
            mHosts = null;
        }

        mHosts = new ArrayList<ModuleHost>();
        ModuleHost host;

        for(ComponentName module: mModules) {
            host = new ModuleHost(mContext.get());
            host.bindTo(module);
            mHosts.add(host);
        }
    }


    /** Query the hosted modules.
    @param query An Intent containing the query that will be processed by
           each module.
    @return a List of IntentWrapper objects, each containing a response from
            one module.
    */
    public List<IntentWrapper> dispatch(Intent query) {
        if(mHosts == null) {
            throw new IllegalStateException(this +
                    " is not hosting any modules to receive " +
                    query);
        }

        List<IntentWrapper> results = new ArrayList<IntentWrapper>();

        for(ModuleHost host: mHosts) {
            try {
                IModule binder = host.getBinder();
                if(binder == null) {
                    Log.w(this.toString(), "binder for " + host +
                            " was not ready in time to receive " +
                            query);
                    continue;
                }

                List<Intent> partial = binder.process(query);
                for(Intent item: partial) {
                    results.add(new IntentWrapper(item));
                }
            }
            catch(RemoteException e) {
                Log.e(this.toString(), e.toString());
            }
        }

        return results;
    }


    /** Notify this dispatcher that the list of enabled modules has changed
    and that the hosting must be refreshed.
    */
    public void notifyEnabledModulesChanged() {
        destroy();
        loadModuleListFromPreferences();
        initializeModuleHosts();
    }


    /** Tear down this dispatcher.
    Unbind from all modules and then forget about them.
    */
    public void destroy() {
        for(ModuleHost host: mHosts) {
            host.unbind();
        }
        mHosts = null;
        mModules = null;
    }
}
