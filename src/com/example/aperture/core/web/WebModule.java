package com.example.aperture.core.web;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


/** Stub module for recognizing URIs.
Pretty dumb. Just sticks "http://" in front of a query and calls it a URI.
*/
public class WebModule extends Module {

    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            public List<Intent> process(Intent data) {

                List<Intent> response = new ArrayList<Intent>();
                String query = data.getStringExtra(Module.QUERY_TEXT);

                // Make sure there's a scheme.
                Uri addr = Uri.parse(query);
                if(addr.isRelative())
                    addr = addr.buildUpon().scheme("http").build();

                // Make an intent to browse.
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                webIntent.setData(addr);
                webIntent.putExtra(Module.RESPONSE_TEXT, addr.toString());

                // Respond.
                response.add(webIntent);
                return response;
            }
        };
    }

}
