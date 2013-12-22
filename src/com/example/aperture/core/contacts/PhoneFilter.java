package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.Module;


public class PhoneFilter extends Filter {

    private final static String[] DATA_PROJECTION = new String[] {
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.DATA1
    };
    private final static String MIMETYPE_SELECTION =
            ContactsContract.Data.MIMETYPE + "=?";
    private final static String[] PHONE_MIMETYPE = new String[] {
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
    };

    private boolean isEntirely12Key(String s) {
        for(char c: s.toCharArray())
            if(!PhoneNumberUtils.is12Key(c))
                return false;
        return true;
    }

    private final static class PhoneComparer implements java.util.Comparator<Intent> {
        private String mFragment;
        public PhoneComparer(String fragment) {
            mFragment = PhoneNumberUtils.stripSeparators(fragment);
        }
        @Override
        public boolean equals(Object o) { return false; }
        @Override
        public int compare(Intent a, Intent b) {
            String x = a.getStringExtra(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            String y = b.getStringExtra(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            x = PhoneNumberUtils.stripSeparators(x);
            y = PhoneNumberUtils.stripSeparators(y);
            int ix = x.indexOf(mFragment);
            int iy = y.indexOf(mFragment);
            if(ix != -1 && iy != -1) {
                if(ix < iy) return -1;
                else if(iy < ix) return 1;
                else return x.compareTo(y);
            }
            else if(ix != -1 && iy == -1) return -1;
            else if(ix == -1 && iy != -1) return 1;
            else return x.compareTo(y);
        }
    }

    private void sort(List<Intent> results, String query) {
        Intent[] buffer = new Intent[results.size()];
        java.util.Arrays.sort(results.toArray(buffer), 0, buffer.length,
                new PhoneComparer(query));
        results.clear();
        for(int i = 0; i < buffer.length; ++i)
            results.add(buffer[i]);
    }

    @Override
    public List<Intent> filter(Context context, String query) {
        List<Intent> results = new ArrayList<Intent>();
        String fragment = PhoneNumberUtils.stripSeparators(query);

        if(!isEntirely12Key(fragment))
            return results;

        Cursor data = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                DATA_PROJECTION,
                MIMETYPE_SELECTION,
                PHONE_MIMETYPE,
                null);

        for(data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            String phone = PhoneNumberUtils.stripSeparators(data.getString(2));
            if(phone.contains(query)) {
                results.add(intentFor(data.getString(1), data.getString(2)));
            }
        }
        data.close();

        sort(results, query);
        return results;
    }

    private Intent intentFor(String displayName, String phone) {
        Intent result = new Intent(Intent.ACTION_DIAL,
                new Uri.Builder().scheme("tel")
                .opaquePart(PhoneNumberUtils.stripSeparators(phone))
                .build());
        result.putExtra(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, phone);
        result.putExtra(Module.RESPONSE_TEXT,
                String.format("%1$s <%2$s>", displayName, phone));
        return result;
    }
}
