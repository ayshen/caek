package com.example.aperture.core.contacts;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;


public class QueryHelper {

    public final static String[] LOOKUP_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
    };

    public final static int _ID_INDEX = 0;
    public final static int LOOKUP_KEY_INDEX = 1;
    public final static int DISPLAY_NAME_PRIMARY_INDEX = 2;

    public final static String[] DATA1_PROJECTION = new String[] {
            ContactsContract.Data.DATA1
    };

    public static Cursor query(Context context,
            Uri uri, String[] projection,
            String selection, String[] selectionArgs,
            String sort) {
        return context.getContentResolver().query(
                uri, projection, selection, selectionArgs, sort);
    }

    public static Cursor contactsLike(String part, Context context) {
        Uri uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_FILTER_URI,
                part);
        return query(context,
                uri,
                LOOKUP_PROJECTION,
                null,
                null,
                null);
    }

    private static Cursor entriesForMimetypeByLookupKey(String lookupKey,
            String mimetype, Context context) {
        String selection = String.format("%1$s = ? AND %2$s = ?",
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Data.MIMETYPE);
        String[] selectionArgs = new String[] { lookupKey, mimetype };
        return query(context,
                ContactsContract.Data.CONTENT_URI,
                DATA1_PROJECTION,
                selection,
                selectionArgs,
                null);
    }

    public static Cursor emailsFor(String lookupKey, Context context) {
        return entriesForMimetypeByLookupKey(lookupKey,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                context);
    }

    public static Cursor phoneNumbersFor(String lookupKey, Context context) {
        return entriesForMimetypeByLookupKey(lookupKey,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                context);
    }
}
