package com.example.aperture.core.email;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


/** Stub module for recognizing email addresses.
Doesn't do any special processing; just assumes that strings that match the
regular expression <code>.*[^@]@.+</code> are email addresses.
*/
public class EmailModule extends Module {

    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            public List<Intent> process(Intent data) {

                List<Intent> response = new ArrayList<Intent>();
                String query = data.getStringExtra(Module.QUERY_TEXT);

                // (Experimental) strip conversational text.
                String preamble = "send an email to ";
                if(query.toLowerCase().startsWith(preamble))
                    query = query.substring(preamble.length());

                // Make sure there's a query.
                if(query == null || query.length() == 0)
                    return response;

                // Find a valid '@', if any.
                int at_index = query.indexOf("@");
                if(at_index <= 0 || at_index >= query.length() - 1)
                    return response;

                // Construct an intent to send an email.
                Uri mailto = new Uri.Builder()
                        .scheme("mailto")
                        .opaquePart(query)
                        .build();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(mailto);
                emailIntent.putExtra(Module.RESPONSE_TEXT, "Send email to " + query);

                // Respond.
                response.add(emailIntent);
                return response;
            }
        };
    }

}
