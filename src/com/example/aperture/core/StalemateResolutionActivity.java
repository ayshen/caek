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


public class StalemateResolutionActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ModulesFragment())
                .commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if(mi.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(mi);
    }

public static class ModulesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.stalemate_resolution);

        List<ResolveInfo> modules = findInstalledModules();

        List<ComponentName> currentlyInstalledModules =
                componentsRepresenting(modules);
        List<ComponentName> previouslyInstalledModules =
                findPreviouslyInstalledModules();

        updateKeys(currentlyInstalledModules,
                previouslyInstalledModules);

        buildPreferenceScreen(modules);
    }


    private void buildPreferenceScreen(List<ResolveInfo> modules) {
        PackageManager pm = getActivity().getPackageManager();
        List<ComponentNameWrapper> installedModules =
                new ArrayList<ComponentNameWrapper>();
        for(ResolveInfo info: modules) {
            installedModules.add(new ComponentNameWrapper(pm, info));
        }

        PreferenceCategory category = null;
        PreferenceScreen screen = getPreferenceScreen();
        if(screen == null) {
            android.util.Log.e(this.toString(), "no PreferenceScreen");
            return;
        }
        else category = (PreferenceCategory)screen.findPreference(
                getActivity().getResources().getString(
                        R.string.resolve_stalemates_category_key));

        for(ComponentNameWrapper name: installedModules) {
            category.addPreference(switchPreferenceFor(name));
        }
    }


    private SwitchPreference switchPreferenceFor(
            ComponentNameWrapper name) {
        SwitchPreference pref = new SwitchPreference(getActivity());
        pref.setKey(name.component.flattenToString());
        pref.setTitle(name.toString());
        pref.setPersistent(true);
        return pref;
    }


    private List<ResolveInfo> findInstalledModules() {
        Intent bindIntent = new Intent(ModuleManagement.ACTION_BIND_MODULE);
        PackageManager pm = getActivity().getPackageManager();
        return pm.queryIntentServices(bindIntent, 0);
    }


    private List<ComponentName> findPreviouslyInstalledModules() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> flatNames = prefs.getStringSet(
                ModuleManagement.LAST_ALL_MODULES, new TreeSet<String>());

        List<ComponentName> components = new ArrayList<ComponentName>();
        for(String name: flatNames) {
            components.add(ComponentName.unflattenFromString(name));
        }

        return components;
    }


    private List<ComponentName> componentsRepresenting(
            List<ResolveInfo> modules) {
        List<ComponentName> components = new ArrayList<ComponentName>();
        for(ResolveInfo info: modules) {
            components.add(new ComponentName(info.serviceInfo.packageName,
                    info.serviceInfo.name));
        }
        return components;
    }


    private void updateKeys(List<ComponentName> now,
            List<ComponentName> before) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        for(ComponentName name: now) {
            if(!before.contains(name)) {
                prefs.edit()
                        .putBoolean(name.flattenToString(), false)
                        .commit();
            }
        }

        for(ComponentName name: before) {
            if(!now.contains(name)) {
                prefs.edit()
                        .remove(name.flattenToString())
                        .commit();
            }
        }

        Set<String> flatNow = new TreeSet<String>();
        for(ComponentName name: now) {
            flatNow.add(name.flattenToString());
        }

        prefs.edit()
                .putStringSet(ModuleManagement.LAST_ALL_MODULES, flatNow)
                .commit();
    }

}

}
