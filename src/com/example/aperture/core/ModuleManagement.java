package com.example.aperture.core;


/** Constants for dealing with installed modules.
Used by {@link com.example.aperture.core.StalemateResolutionActivity} and
{@link com.example.aperture.core.ModuleHost} to handle changes in the installed
modules and connections to modules respectively.
*/
public class ModuleManagement {

    /** Name of shared preferences key for the list of installed modules the
    last time {@link com.example.aperture.core.StalemateResolutionActivity} was
    started.
    */
    public final static String LAST_ALL_MODULES =
            "com.example.aperture.core.LAST_ALL_MODULES";

    /** Action used with an Intent for binding to a module.
    */
    public final static String ACTION_BIND_MODULE =
            "com.example.aperture.core.ACTION_BIND_MODULE";
}
