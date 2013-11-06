package com.example.aperture.core.contacts;

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

import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.IModule;
import com.example.aperture.core.Module;


public class ContactsModule extends Module {

    protected final static String[] DATA1_PROJECTION = new String[] {
            ContactsContract.Data.DATA1
    };

    @Override
    protected IBinder createBinder() {
        return new IModule.Stub() {
            private int indexOfStringInArray(String s, String[] array) {
                for(int i = 0; i < array.length; ++i)
                    if(array[i].equals(s))
                        return i;
                return -1;
            }

            private Uri createLookupUri(long id, String lookup) {
                return ContactsContract.Contacts.lookupContact(
                        ContactsModule.this.getContentResolver(),
                        ContactsContract.Contacts.getLookupUri(
                                id, lookup));
            }

            private Intent createViewIntent(Cursor c, String[] projection) {
                int idIndex = indexOfStringInArray(
                        ContactsContract.Contacts._ID, projection);
                int lookupIndex = indexOfStringInArray(
                        ContactsContract.Contacts.LOOKUP_KEY, projection);
                int displayNameIndex = indexOfStringInArray(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        projection);

                Uri viewUri = createLookupUri(c.getLong(idIndex),
                        c.getString(lookupIndex));

                Intent viewIntent = new Intent(Intent.ACTION_VIEW, viewUri);
                viewIntent.putExtra(Module.RESPONSE_TEXT,
                        c.getString(displayNameIndex));
                return viewIntent;
            }

            private Intent guessPhone(String query, Cursor c,
                    String[] projection) {

                if(PhoneNumberUtils.stripSeparators(query).length() == 0)
                    return null;

                // A phone number should be entirely dialable.
                boolean queryIsDialable = true;
                char[] q = PhoneNumberUtils.stripSeparators(query)
                        .toCharArray();
                for(char a: q)
                    queryIsDialable &= PhoneNumberUtils.isReallyDialable(a);

                if(!queryIsDialable)
                    return null;

                // Looks like a phone number. Let's try to grab the full
                // phone number from the contacts database.
                int idIndex = indexOfStringInArray(
                        ContactsContract.Contacts._ID, projection);
                int lookupIndex = indexOfStringInArray(
                        ContactsContract.Contacts.LOOKUP_KEY, projection);
                int displayNameIndex = indexOfStringInArray(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        projection);

                //Uri dburi = createLookupUri(c.getLong(idIndex),
                        //c.getString(lookupIndex));
                String filter = ContactsContract.Contacts.LOOKUP_KEY +
                        " =? AND " + ContactsContract.Data.MIMETYPE + " =?";
                String[] params = new String[] {
                        c.getString(lookupIndex),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                };

                Cursor c2 = ContactsModule.this.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        ContactsModule.DATA1_PROJECTION, filter, params, null);

                if(c2 == null) {
                    // Query failed.
                    return null;
                }

                if(c2.getCount() == 0) {
                    // No results.
                    c2.close();
                    return null;
                }

                for(c2.moveToFirst(); !c2.isAfterLast(); c2.moveToNext()) {
                    if(PhoneNumberUtils.stripSeparators(c2.getString(0))
                            .indexOf(PhoneNumberUtils.stripSeparators(query))
                            == -1) {
                        continue;
                    }

                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    dialIntent.setData(new Uri.Builder().scheme("tel")
                            .opaquePart(c2.getString(0)).build());
                    dialIntent.putExtra(Module.RESPONSE_TEXT,
                            "Call " + c.getString(displayNameIndex) +
                            " at " + c2.getString(0));
                    c2.close();
                    return dialIntent;
                }

                return null;
            }

            private Intent guessEmail(String query, Cursor c,
                    String[] projection) {
                int idIndex = indexOfStringInArray(
                        ContactsContract.Contacts._ID, projection);
                int lookupIndex = indexOfStringInArray(
                        ContactsContract.Contacts.LOOKUP_KEY, projection);
                int displayNameIndex = indexOfStringInArray(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        projection);

                String filter = ContactsContract.Contacts.LOOKUP_KEY +
                        " =? AND " + ContactsContract.Data.MIMETYPE + " =?";
                String[] params = new String[] {
                    c.getString(lookupIndex),
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                };

                Cursor c2 = ContactsModule.this.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        ContactsModule.DATA1_PROJECTION, filter, params, null);

                if(c2 == null) {
                    // Query failed.
                    return null;
                }

                if(c2.getCount() == 0) {
                    // No results.
                    c2.close();
                    return null;
                }

                for(c2.moveToFirst(); !c2.isAfterLast(); c2.moveToNext()) {
                    if(!c2.getString(0).startsWith(query)) {
                        continue;
                    }

                    Intent sendtoIntent = new Intent(Intent.ACTION_SENDTO);
                    sendtoIntent.setData(new Uri.Builder().scheme("mailto")
                            .opaquePart(c2.getString(0)).build());
                    sendtoIntent.putExtra(Module.RESPONSE_TEXT,
                            c.getString(displayNameIndex) + " (" +
                            c2.getString(0) + ")");
                    c2.close();
                    return sendtoIntent;
                }

                return null;
            }

            private Intent guessQuickAction(String query, Cursor c,
                    String[] projection) {
                Intent guess = null;

                if((guess = guessEmail(query, c, projection)) != null)
                    return guess;
                if((guess = guessPhone(query, c, projection)) != null)
                    return guess;

                return createViewIntent(c, projection);
            }

            public List<Intent> process(Intent data) {
                List<Intent> results = new ArrayList<Intent>();
                String query = data.getStringExtra(Module.QUERY_TEXT);

                Uri filterUri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_FILTER_URI,
                        Uri.encode(query));
                String[] projection = new String[] {
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts._ID
                };

                Cursor c = ContactsModule.this.getContentResolver().query(
                        filterUri, projection, null, null, null);

                if(c == null) {
                    // Query failed. Decline to respond.
                    return results;
                }

                if(c.getCount() == 0) {
                    // Query succeeded, but no matches were found.
                    // Decline to respond.
                    c.close();
                    return results;
                }

                if(c.getCount() == 1) {
                    // There's only one person. Can we make things quicker
                    // (e.g. was the user typing one of the person's contact
                    // methods)?
                    c.moveToFirst();
                    Intent quickActionGuess = guessQuickAction(query, c,
                            projection);
                    if(quickActionGuess != null) {
                        results.add(quickActionGuess);
                    }
                    else {
                        results.add(createViewIntent(c, projection));
                    }
                }
                else {
                    // Make a people-viewer intent for each candidate.
                    for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        results.add(createViewIntent(c, projection));
                    }
                }

                // Always clean up after yourself!
                c.close();
                return results;
            }
        };
    }

}
