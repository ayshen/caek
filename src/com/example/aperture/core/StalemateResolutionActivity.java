package com.example.aperture.core;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import android.view.MenuItem;

import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/** Class for managing which modules get to analyze content queries.
Each module gets a switch to enable or disable, which is persisted in shared
preferences. The preferences are read by the main activity to determine which
modules to host.
*/
public class StalemateResolutionActivity extends PreferenceActivity {


    /** Called when this activity is created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ModulesFragment())
                .commit();
    }


    /** Handle the Up affordance being pressed. */
    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if(mi.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(mi);
    }


    /** Fragment that handles all the individual module preferences.
    We do this because a modern, forward-thinking application should never
    rely on deprecated APIs, such as the ones that do what is done in this
    fragment at the activity level, to get things done.
    */
    public static class ModulesFragment extends PreferenceFragment {


        /** Called when this fragment is created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.stalemate_resolution);

            // Find out which modules are currently installed.
            List<ResolveInfo> modules = findInstalledModules();
            List<ComponentName> currentlyInstalledModules =
                    componentsRepresenting(modules);

            // Load the list of modules that was installed the last time this
            // fragment was created.
            List<ComponentName> previouslyInstalledModules =
                    findPreviouslyInstalledModules();

            // Find out what's changed since the last time this fragment was
            // created (new modules, uninstalled modules).
            updateKeys(currentlyInstalledModules,
                    previouslyInstalledModules);

            buildPreferenceScreen(modules);
        }


        /** Construct the preference views for each module.
        @param modules The list of modules that is currently installed.
        */
        private void buildPreferenceScreen(List<ResolveInfo> modules) {

            // Convert the ResolveInfo objects into components, and also
            // figure out what to call them.
            PackageManager pm = getActivity().getPackageManager();
            List<ComponentNameWrapper> installedModules =
                    new ArrayList<ComponentNameWrapper>();
            for(ResolveInfo info: modules) {
                installedModules.add(new ComponentNameWrapper(pm, info));
            }

            // Get the pre-inflated category from the screen so we can add
            // switch preferences to it.
            PreferenceCategory category = null;
            PreferenceScreen screen = getPreferenceScreen();
            if(screen == null) {
                android.util.Log.e(this.toString(), "no PreferenceScreen");
                return;
            }
            else category = (PreferenceCategory)screen.findPreference(
                    getActivity().getResources().getString(
                            R.string.resolve_stalemates_category_key));

            // Create switch preferences for each module.
            for(ComponentNameWrapper name: installedModules) {
                category.addPreference(switchPreferenceFor(name));
            }
        }


        /** Create a switch preference for a module.
        @param name A wrapper around the component representing the module.
        @return a SwitchPreference for the module.
        */
        private SwitchPreference switchPreferenceFor(
                ComponentNameWrapper name) {
            SwitchPreference pref = new SwitchPreference(getActivity());
            pref.setKey(name.component.flattenToString());
            pref.setTitle(name.toString());
            pref.setPersistent(true);
            return pref;
        }


        /** Query the system package manager for things that look like they
        will work like modules.
        @return a List of ResolveInfo objects, each representing a service that
        declares that it accepts the action
        <code>com.example.aperture.core.ACTION_BIND_MODULE</code>.
        */
        private List<ResolveInfo> findInstalledModules() {
            Intent bindIntent = new Intent(ModuleManagement.ACTION_BIND_MODULE);
            PackageManager pm = getActivity().getPackageManager();
            return pm.queryIntentServices(bindIntent, 0);
        }


        /** Load from shared preferences a list of the modules that were
        installed the last time this fragment was created.
        @return a List of ComponentNames, each of which may or may not
        correspond to a package currently installed on the system, but which
        represented a module when they were flattened into shared preferences.
        */
        private List<ComponentName> findPreviouslyInstalledModules() {
            // Load the component names from shared preferences.
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            Set<String> flatNames = prefs.getStringSet(
                    ModuleManagement.LAST_ALL_MODULES, new TreeSet<String>());

            // Unflatten the component names.
            List<ComponentName> components = new ArrayList<ComponentName>();
            for(String name: flatNames) {
                components.add(ComponentName.unflattenFromString(name));
            }

            return components;
        }


        /** Convert ResolveInfo objects into ComponentName objects.
        @param modules a List of ResolveInfo objects representing components.
        @return the ComponentName representations of the ResolveInfo objects.
        */
        private List<ComponentName> componentsRepresenting(
                List<ResolveInfo> modules) {
            List<ComponentName> components = new ArrayList<ComponentName>();
            for(ResolveInfo info: modules) {
                components.add(new ComponentName(info.serviceInfo.packageName,
                        info.serviceInfo.name));
            }
            return components;
        }


        /** Determine what changes to the list of installed modules have
        occurred since the last time this fragment was created.
        Also cache the list of currently installed modules in shared
        preferences, so that this operation can be performed on up-to-date
        data the next time this fragment is created.
        @param now The List of module components that is installed now
        @param before The List of module components that was installed the
        last time this fragment was created
        */
        private void updateKeys(List<ComponentName> now,
                List<ComponentName> before) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Check for new modules. New modules are disabled by default.
            for(ComponentName name: now) {
                if(!before.contains(name)) {
                    prefs.edit()
                            .putBoolean(name.flattenToString(), false)
                            .commit();
                }
            }

            // Check for uninstalled modules and clean up their preference
            // keys, so that we don't litter the shared preferences with
            // old component names.
            for(ComponentName name: before) {
                if(!now.contains(name)) {
                    prefs.edit()
                            .remove(name.flattenToString())
                            .commit();
                }
            }

            // Flatten the current list of installed modules.
            Set<String> flatNow = new TreeSet<String>();
            for(ComponentName name: now) {
                flatNow.add(name.flattenToString());
            }

            // Cache the current list of installed modules.
            prefs.edit()
                    .putStringSet(ModuleManagement.LAST_ALL_MODULES, flatNow)
                    .commit();
        }

    }

}
