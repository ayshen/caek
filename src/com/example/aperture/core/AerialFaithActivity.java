package com.example.aperture.core;

import android.app.Activity;
import android.app.ListActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;


public class AerialFaithActivity extends ListActivity {

    private final static int REQUEST_SPEECH = 1;
    private final static int REQUEST_IMAGE = 2;
    private final static int REQUEST_VIDEO = 4;


    private Set<ComponentName> mModules = new TreeSet<ComponentName>();

    private RelativeLayout header = null;
    private SearchView polybox = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeHeaderView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadModuleListFromPreferences();
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
        ArrayList<String> components = new ArrayList<String>();
        for(String name: modules) {
            if(prefs.getBoolean(name, false))
                components.add(name);
        }
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                components));
    }


    private void initializeHeaderView() {
        header = (RelativeLayout)getLayoutInflater().inflate(
                R.layout.main, getListView(), false);
        getListView().addHeaderView(header);

        polybox = (SearchView)header.findViewById(android.R.id.text1);
        polybox.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_URI);

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

}
