package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.List;


public abstract class Filter {
    public abstract List<Intent> filter(Context context, String query);

    protected Uri createLookupUri(Context context, String lookupKey) {
        return ContactsContract.Contacts.getLookupUri(
                context.getContentResolver(),
                Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                        Uri.encode(lookupKey)));
    }
}
