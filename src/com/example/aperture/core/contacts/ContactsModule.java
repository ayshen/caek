package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


public class ContactsModule extends Module {

    private final static Object[] FILTERS = new Object[] {
            EmailFilter.class,
            PhoneFilter.class
    };

    @Override
    public IBinder createBinder() {
        return new ContactsModule.Binder(this);
    }

    private final static class Binder extends IModule.Stub {

        private final static String[] CONTENT_FILTER_PROJECTION =
                new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        };

        private WeakReference<ContactsModule> mContext;

        public Binder(ContactsModule context) {
            mContext = new WeakReference<ContactsModule>(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Intent> process(Intent data) {
            List<Intent> results = new ArrayList<Intent>();
            String query = data.getStringExtra(Module.QUERY_TEXT);

            for(int i = 0; i < FILTERS.length; ++i) {
                try {
                    Class<? extends Filter> filterClass = (Class<? extends Filter>)FILTERS[i];
                    Filter filter = filterClass.newInstance();
                    results.addAll(filter.filter(mContext.get(), query));
                }
                catch(ClassCastException ecas) {}
                catch(IllegalAccessException eilx) {}
                catch(InstantiationException enew) {}
            }

            results.addAll(viewIntentsFor(contactsLike(query)));

            return results;
        }

        private List<Intent> viewIntentsFor(Cursor contacts) {
            List<Intent> results = new ArrayList<Intent>();
            for(contacts.moveToFirst(); !contacts.isAfterLast();
                    contacts.moveToNext()) {
                results.add(viewIntentFor(contacts.getLong(0),
                        contacts.getString(1), contacts.getString(2)));
            }
            contacts.close();
            return results;
        }

        private Intent viewIntentFor(long id, String lookupKey,
                String displayNamePrimary) {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(ContactsContract.Contacts.lookupContact(
                    mContext.get().getContentResolver(),
                    ContactsContract.Contacts.getLookupUri(id, lookupKey)));
            viewIntent.putExtra(Module.RESPONSE_TEXT, displayNamePrimary);
            return viewIntent;
        }

        private Cursor contactsLike(String query) {
            return mContext.get().getContentResolver().query(
                    Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                            Uri.encode(query)),
                    CONTENT_FILTER_PROJECTION,
                    null, null, null);
        }
    }
}
