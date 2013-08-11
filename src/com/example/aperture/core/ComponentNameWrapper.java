package com.example.aperture.core;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


public class ComponentNameWrapper {

    private final String name;
    public final ComponentName component;

    public ComponentNameWrapper(PackageManager pm, ResolveInfo info) {
        try {
            name = pm.getText(
                    info.serviceInfo.packageName,
                    info.serviceInfo.labelRes,
                    info.serviceInfo.applicationInfo).toString();
        }
        catch(Exception e) {
            name = info.serviceInfo.name;
        }
        component = new ComponentName(
                info.serviceInfo.packageName,
                info.serviceInfo.name);
    }

    public String toString() {
        return name;
    }
}
