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
import android.preference.PreferenceFragment;
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
    }


    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        loadHeadersFromResource(R.xml.stalemate_resolution_headers, target);
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

            if(getActivity() == null)
                throw new IllegalStateException(this.toString() +
                        ".onCreate() called before attaching to an activity");

            PackageManager pm = getActivity().getPackageManager();
            Intent bindIntent = new Intent(ModuleManagement.ACTION_BIND_MODULE);
            List<ResolveInfo> modules = pm.queryIntentServices(bindIntent, 0);
        }

    }

}
