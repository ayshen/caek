package com.example.aperture.core;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


/** Wrapper class for ComponentName objects.
This used to be part of a debug system where the modules were presented in a
ListView, but that has since been removed. Now they're just convenient for
storing a module's label.

<em>Beware! This class may disappear without warning.</em>
*/
public class ComponentNameWrapper {

    private final String name;
    public final ComponentName component;

    public ComponentNameWrapper(PackageManager pm, ResolveInfo info) {
        String n;
        try {
            n = pm.getText(
                    info.serviceInfo.packageName,
                    info.serviceInfo.labelRes,
                    info.serviceInfo.applicationInfo).toString();
        }
        catch(Exception e) {
            n= info.serviceInfo.name;
        }
        name = n;
        component = new ComponentName(
                info.serviceInfo.packageName,
                info.serviceInfo.name);
    }

    public String toString() {
        return name;
    }
}
