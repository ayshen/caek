package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.Module;


public class EmailFilter extends Filter {

    private final static String[] DATA_PROJECTION = new String[] {
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.DATA1
    };
    private final static String MIMETYPE_SELECTION =
            ContactsContract.Data.MIMETYPE + "=?";
    private final static String[] EMAIL_MIMETYPE = new String[] {
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
    };

    private final static String[] PREFIXES = new String[] {
            "send an email to ",
            "send email to ",
            "email "
    };

    private final static class EmailComparer implements java.util.Comparator<Intent> {
        private String mFragment;
        public EmailComparer(String fragment) {
            mFragment = fragment;
        }
        @Override
        public boolean equals(Object o) { return false; }
        @Override
        public int compare(Intent a, Intent b) {
            // Get the emails from the intents.
            String x = a.getStringExtra(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            String y = b.getStringExtra(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

            // Split the emails into usernames and domains.
            String xu = x.substring(0, x.indexOf('@'));
            String yu = y.substring(0, y.indexOf('@'));
            String xd = x.substring(x.indexOf('@')+1);
            String yd = y.substring(y.indexOf('@')+1);

            // Get the locations of the query in the usernames and domains.
            int ixu = xu.indexOf(mFragment), iyu = yu.indexOf(mFragment);
            int ixd = xd.indexOf(mFragment), iyd = yd.indexOf(mFragment);

            // TODO refactor this to be less iffy
            // Explicitly writing out the comparision logic to avoid violating
            // general sorting contract for total ordering.
            if(ixu != -1 && iyu != -1) {
                if(ixu < iyu) return -1;
                else if(iyu < ixu) return 1;
                else return xu.compareTo(yu);
            }
            else if(ixu != -1 && iyu == -1) {
                return -1;
            }
            else if(ixu == -1 && iyu != -1) {
                return 1;
            }
            else {
                if(ixd != -1 && iyd != -1) {
                    if(ixd < iyd) return -1;
                    else if(iyd < ixd) return 1;
                    else return xd.compareTo(yd);
                }
                else if(ixd != -1 && iyd == -1) {
                    return -1;
                }
                else if(ixd == -1 && iyd != -1) {
                    return 1;
                }
                else
                    return x.compareTo(y);
            }
        }
    }

    private void sort(List<Intent> results, String query) {
        Intent[] buffer = new Intent[results.size()];
        java.util.Arrays.sort(results.toArray(buffer), 0, buffer.length,
                new EmailComparer(query));
        results.clear();
        for(int i = 0; i < buffer.length; ++i)
            results.add(buffer[i]);
    }

    @Override
    public List<Intent> filter(Context context, String query) {
        List<Intent> results = new ArrayList<Intent>();

        boolean hasPrefix = false;
        for(String prefix: PREFIXES) {
            if(query.startsWith(prefix)) {
                query = query.substring(prefix.length());
                hasPrefix = true;
                break;
            }
        }

        Cursor data = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                DATA_PROJECTION,
                MIMETYPE_SELECTION,
                EMAIL_MIMETYPE,
                null);

        if(hasPrefix)
            results.addAll(filterByName(data, query));
        results.addAll(filterByAddress(data, query));
        data.close();

        return results;
    }

    private List<Intent> filterByName(Cursor data, String query) {
        List<Intent> results = new ArrayList<Intent>();
        query = query.toLowerCase();
        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String name = data.getString(1).toLowerCase();
            if(name.contains(query))
                results.add(intentFor(data.getString(1), data.getString(2)));
        }
        return results;
    }

    private List<Intent> filterByAddress(Cursor data, String query) {
        List<Intent> results = new ArrayList<Intent>();
        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String email = data.getString(2);
            if(email.contains(query))
                results.add(intentFor(data.getString(1), email));
        }
        sort(results, query);
        return results;
    }

    private Intent intentFor(String displayName, String email) {
        Intent result = new Intent(Intent.ACTION_SENDTO,
                new Uri.Builder().scheme("mailto").opaquePart(email).build());
        result.putExtra(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, email);
        result.putExtra(Module.RESPONSE_TEXT,
                String.format("%1$s <%2$s>", displayName, email));
        return result;
    }
}
