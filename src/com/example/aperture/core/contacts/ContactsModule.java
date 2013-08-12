package com.example.aperture.core.contacts;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.IBinder;

import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


public class ContactsModule extends Module {


    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            public List<Intent> process(Intent data) {
                List<Intent> results = new ArrayList<Intent>();
                String query = data.getStringExtra(Module.QUERY_TEXT);

                Uri filterUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_FILTER_URI,
                        Uri.encode(query));
                String[] projection = new String[] {
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.LOOKUP_KEY
                };

                Cursor c = ContactsModule.this.getContentResolver().query(
                        filterUri, projection, null, null, null);
                if(c == null) return results;
                if(c.getCount() == 0) {
                    c.close();
                    return results;
                }

                for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    Uri viewUri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                            c.getString(1));
                    Intent contactIntent = new Intent(Intent.ACTION_VIEW,
                            viewUri);
                    contactIntent.putExtra(Module.RESPONSE_TEXT,
                            c.getString(0));
                    results.add(contactIntent);
                }
                return results; // TODO
            }
        };
    }

}
