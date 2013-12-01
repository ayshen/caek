package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ArrayList;

import com.example.aperture.core.Module;


public class PhoneFilter extends QuickActionFilter
{
    public PhoneFilter(Context context)
    {
        mContext = new WeakReference<Context>(context);
    }

    @Override
    public List<Intent> filter(String query)
    {
        List<Intent> results = new ArrayList<Intent>();

        if(query.trim().length() == 0) return results;

        String[] prefixes = new String[]
        {
            "make a telephone call to ",
            "place a telephone call to ",
            "make a call to ",
            "place a call to ",
            "call "
        };
        for(String prefix: prefixes)
        {
            if(query.toLowerCase().startsWith(prefix))
                query = query.substring(prefix.length());
        }

        String q = PhoneNumberUtils.stripSeparators(query);

        boolean queryIsDialable = true;
        for(char c: q.toCharArray())
            queryIsDialable &= PhoneNumberUtils.isReallyDialable(c);
        if(!queryIsDialable) return results;

        for(IterableCursor c = new IterableCursor(
                QueryHelper.contactsLike(q, mContext.get()));
                c.hasNext(); )
        {
            Cursor contact = c.next();
            String lookupKey = contact.getString(QueryHelper.LOOKUP_KEY_INDEX);
            for(IterableCursor c2 = new IterableCursor(QueryHelper.phoneNumbersFor(
                    lookupKey, mContext.get())); c2.hasNext(); )
            {
                Cursor phone = c2.next();
                String num = PhoneNumberUtils.stripSeparators(phone.getString(0));
                if(num.startsWith(q))
                    results.add(makeQuickActionIntent(
                            contact.getString(QueryHelper.DISPLAY_NAME_PRIMARY_INDEX),
                            num));
            }
        }

        if(results.size() == 0)
        {
            for(char c: query.toCharArray())
                if("0123456789*#-,;".indexOf(c) == -1)
                    return results;

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(new Uri.Builder().scheme("tel").opaquePart(query).build());
            intent.putExtra(Module.RESPONSE_TEXT, "-Dial " + query);
            results.add(intent);
        }

        return results;
    }

    private Intent makeQuickActionIntent(String displayNamePrimary, String num)
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(new Uri.Builder().scheme("tel").opaquePart(num).build());
        intent.putExtra(Module.RESPONSE_TEXT,
                String.format("-Call %1$s at %2$s",
                displayNamePrimary,
                num));
        return intent;
    }
}
