package com.example.aperture.core.phone;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.List;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


/** Stub module for recognizing phone numbers.
Extremely lazy. Only checks for sequences of dialable characters.
*/
public class PhoneModule extends Module {

    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            public List<Intent> process(Intent data) {

                List<Intent> response = new ArrayList<Intent>();
                String query = data.getStringExtra(Module.QUERY_TEXT);

                // Ignore empty strings.
                if(query == null || query.length() == 0)
                    return response;

                // (experimental) Strip conversational text.
                String preamble = "call ";
                if(query.toLowerCase().startsWith(preamble))
                    query = query.substring(preamble.length());
                preamble = "tel:";
                if(query.startsWith(preamble))
                    query = query.substring(preamble.length());

                // Check the query.
                query = PhoneNumberUtils.stripSeparators(query);
                for(char c: query.toCharArray())
                    if(!PhoneNumberUtils.is12Key(c))
                        return response;

                // Check the query again.
                if(PhoneNumberUtils.formatNumber(query).length() == 0)
                    return response;

                // Build a dialer intent, so that the user can confirm the
                // number in the dialer before the call is placed.
                Uri tel = new Uri.Builder()
                        .scheme("tel")
                        .opaquePart(query)
                        .build();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(tel);
                callIntent.putExtra(Module.RESPONSE_TEXT, "Call " +
                        PhoneNumberUtils.formatNumber(query));

                // Respond.
                response.add(callIntent);
                return response;
            }
        };
    }

}
