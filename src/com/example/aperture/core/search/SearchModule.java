package com.example.aperture.core.search;

import android.app.SearchManager;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


/** Stub module for performing searches.
The dumbest of all possible modules. Doesn't even look at the query; just makes
an intent to search for it.
*/
public class SearchModule extends Module {

    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            public List<Intent> process(Intent data) {

                List<Intent> response = new ArrayList<Intent>();
                Intent searchIntent = new Intent(Intent.ACTION_SEARCH);
                String query = data.getStringExtra(Module.QUERY_TEXT);

                searchIntent.putExtra(SearchManager.QUERY, query);
                searchIntent.putExtra(Module.RESPONSE_TEXT, "Search for " + query);

                response.add(searchIntent);
                return response;
            }
        };
    }

}
