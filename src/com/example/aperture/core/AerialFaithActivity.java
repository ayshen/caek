package com.example.aperture.core;

import android.app.Activity;
import android.app.ListActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.RemoteException;

import android.preference.PreferenceManager;

import android.provider.MediaStore;

import android.speech.RecognizerIntent;

import android.text.InputType;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.ToggleButton;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;


public class AerialFaithActivity extends ListActivity
        implements SearchView.OnQueryTextListener {

    private final static int REQUEST_SPEECH = 1;
    private final static int REQUEST_IMAGE = 2;
    private final static int REQUEST_VIDEO = 4;


    private List<ComponentName> mModules = null;

    // TODO make a host pool to run several module queries in parallel.
    private ModuleHost[] mHosts = null;

    private RelativeLayout header = null;
    private SearchView polybox = null;

    private List<IntentWrapper> results = new ArrayList<IntentWrapper>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeHeaderView();
        setListAdapter(new ArrayAdapter<IntentWrapper>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                results));
    }


    public void onStart() {
        super.onStart();
        initializeModuleHosts();
    }


    public void onStop() {
        super.onStart();
        for(ModuleHost host: mHosts) {
            host.unbind();
        }
        mHosts = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(polybox.getQuery().toString().trim().length() > 0) {
            initializeModuleHosts();
            // This is a terrible hack. If there are many modules, this may not
            // be enough time for the services to bind.
            // FIXME
            new android.os.Handler().postDelayed(new Runnable() {
                public void run() {
                    AerialFaithActivity.this.onQueryTextChange(
                            AerialFaithActivity.this.polybox.getQuery().toString());
                }
            }, 100);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.main, m);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if(mi.getItemId() == R.id.resolve_stalemates) {
            Intent resolveStalematesIntent = new Intent(
                    this, StalemateResolutionActivity.class);
            startActivity(resolveStalematesIntent);
            return true;
        }
        return super.onOptionsItemSelected(mi);
    }


    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        if(response != Activity.RESULT_OK) {
            return;
        }

        if(request == REQUEST_SPEECH) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if(results == null || results.size() == 0) {
                return;
            }
            String bestMatch = results.get(0);
            polybox.setQuery(bestMatch, false);
        }
        else if(request == REQUEST_IMAGE) {
            android.widget.Toast.makeText(this, "NotImplemented (img)",
            android.widget.Toast.LENGTH_SHORT).show();
        }
        else if(request == REQUEST_VIDEO) {
            android.widget.Toast.makeText(this, "NotImplemented (vid)",
            android.widget.Toast.LENGTH_SHORT).show();
        }
    }


    private void loadModuleListFromPreferences() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> modules = prefs.getStringSet(
                ModuleManagement.LAST_ALL_MODULES, new TreeSet<String>());
        mModules = new ArrayList<ComponentName>();
        for(String name: modules) {
            if(prefs.getBoolean(name, false))
                mModules.add(ComponentName.unflattenFromString(name));
        }
    }


    private void initializeModuleHosts() {
        // TODO make a host pool to make queries in parallel.
        if(mHosts != null)
            for(ModuleHost host: mHosts)
                host.unbind();
        loadModuleListFromPreferences();
        mHosts = new ModuleHost[mModules.size()];
        for(int i = 0; i < mModules.size(); ++i) {
            mHosts[i] = new ModuleHost(this);
            mHosts[i].bindTo(mModules.get(i));
        }
    }


    private void initializeHeaderView() {
        header = (RelativeLayout)getLayoutInflater().inflate(
                R.layout.main, getListView(), false);
        getListView().addHeaderView(header);

        polybox = (SearchView)header.findViewById(android.R.id.text1);
        polybox.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_URI);

        // @android:id/text1 : text queries
        polybox.setOnQueryTextListener(this);

        // @android:id/button1
        // @android:id/button2
        // keyboard type
        RadioGroup grp = (RadioGroup)header.findViewById(R.id.text1_inputtype);
        grp.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int id) {
                if(id == android.R.id.button1) {
                    AerialFaithActivity.this.polybox.setInputType(
                            InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_URI);
                }
                else if(id == android.R.id.button2) {
                    AerialFaithActivity.this.polybox.setInputType(
                            InputType.TYPE_CLASS_PHONE);
                }
            }
        });

        // @id/button3 : speech recognition
        Button b3 = (Button)header.findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent speechIntent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                startActivityForResult(speechIntent, REQUEST_SPEECH);
            }
        });

        // @id/button4 : image
        Button b4 = (Button)header.findViewById(R.id.button4);
        b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE);
            }
        });

        // @id/button5 : video
        Button b5 = (Button)header.findViewById(R.id.button5);
        b5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(
                        MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_VIDEO);
            }
        });
    }


    @Override
    public boolean onQueryTextChange(String query) {
        // TODO implement a host pool to run module queries in parallel.

        results.clear();

        if(query.trim().length() == 0) {
            ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
            return true;
        }

        Intent moduleQueryIntent = new Intent();
        moduleQueryIntent.putExtra(Module.QUERY_TEXT, query);
        for(ModuleHost host: mHosts) {
            try {
                IModule binder = host.getBinder();
                if(binder == null) {
                    android.util.Log.w(this.toString(), "binder for " + host +
                            " was not ready in time for onQueryTextChange()");
                    continue;
                }
                List<Intent> partial = binder.process(moduleQueryIntent);
                for(Intent intent: partial) {
                    results.add(new IntentWrapper(intent));
                }
            }
            catch(Exception e) {
                android.util.Log.e(this.toString(), e.toString());
            }
        }
        ((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return true; // No effect.
    }


    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        IntentWrapper wrapper = (IntentWrapper)
                getListView().getItemAtPosition(position);
        polybox.setQuery("", false);
        // TODO notify the module that its response was selected?
        startActivity(wrapper.mIntent);
    }

}
