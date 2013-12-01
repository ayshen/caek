package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.os.IBinder;
import android.telephony.PhoneNumberUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


public class ContactsModule extends Module {
    @Override
    public IBinder createBinder() {
        return new ContactsModule.Binder(this);
    }

    private final static class Binder extends IModule.Stub {
        private WeakReference<ContactsModule> mContext;

        public Binder(ContactsModule context) {
            mContext = new WeakReference<ContactsModule>(context);
        }

        @Override
        public List<Intent> process(Intent data) {
//android.util.Log.i(this.toString(), "process() started: " + System.nanoTime());
            List<Intent> results = new ArrayList<Intent>();
            String query = data.getStringExtra(Module.QUERY_TEXT);

//android.util.Log.i(this.toString(), "starting email filtering: " + System.nanoTime());
            results.addAll(new EmailFilter(mContext.get()).filter(query));
//android.util.Log.i(this.toString(), "starting phone filtering: " + System.nanoTime());
            results.addAll(new PhoneFilter(mContext.get()).filter(query));
//android.util.Log.i(this.toString(), "quick action filtering produced " + results.size() + " results: " + System.nanoTime());

            if(results.size() == 0)
                results.addAll(this.contactsLike(query));

//android.util.Log.i(this.toString(), "process() completed: " + System.nanoTime());
            return results;
        }

        private List<Intent> contactsLike(String query) {
            List<Intent> results = new ArrayList<Intent>();
            for(IterableCursor c = new IterableCursor(
                    QueryHelper.contactsLike(query, mContext.get()));
                    c.hasNext(); ) {
                Cursor contact = c.next();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(createLookupUri(contact, mContext.get()));
                intent.putExtra(Module.RESPONSE_TEXT,
                        contact.getString(QueryHelper.DISPLAY_NAME_PRIMARY_INDEX));
                results.add(intent);
            }
            return results;
        }

        private Uri createLookupUri(Cursor contact, Context context) {
            return ContactsContract.Contacts.lookupContact(
                    context.getContentResolver(),
                    ContactsContract.Contacts.getLookupUri(
                            contact.getLong(QueryHelper._ID_INDEX),
                            contact.getString(QueryHelper.LOOKUP_KEY_INDEX)));
        }
    }
}
