package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.example.aperture.core.Module;


public class EmailFilter extends QuickActionFilter
{
    public EmailFilter(Context context)
    {
        mContext = new WeakReference<Context>(context);
    }

    @Override
    public List<Intent> filter(String query)
    {
android.util.Log.i(this.toString(), "filter() started: " + System.nanoTime());
        List<Intent> results = new ArrayList<Intent>();

        if(query.trim().length() == 0) return results;

        String[] prefixes = new String[]
        {
                "send an email to ",
                "send email to ",
                "email "
        };
        for(String prefix: prefixes)
        {
            if(query.toLowerCase().startsWith(prefix))
                query = query.substring(prefix.length());
        }

android.util.Log.i(this.toString(), "starting filtered contacts loop: " + System.nanoTime());
        for(IterableCursor c = new IterableCursor(
                QueryHelper.contactsLike(query, mContext.get()));
                c.hasNext(); )
        {
android.util.Log.i(this.toString(), "next contact: " + System.nanoTime());
            Cursor contact = c.next();
            String lookupKey = contact.getString(QueryHelper.LOOKUP_KEY_INDEX);
            Cursor emails = QueryHelper.emailsFor(lookupKey, mContext.get());

android.util.Log.i(this.toString(), "starting emails loop: " + System.nanoTime());
            for(IterableCursor c2 = new IterableCursor(emails); c2.hasNext(); )
            {
android.util.Log.i(this.toString(), "next email: " + System.nanoTime());
                Cursor email = c2.next();
                String addr = email.getString(0);
                if(addr.startsWith(query))
                    results.add(makeQuickActionIntent(
                            contact.getString(QueryHelper.DISPLAY_NAME_PRIMARY_INDEX),
                            addr));
            }
android.util.Log.i(this.toString(), "completed emails loop: " + System.nanoTime());
        }
android.util.Log.i(this.toString(), "completed filtered contacts loop with " + results.size() + " matches: " + System.nanoTime());

        if(results.size() == 0)
        {
android.util.Log.i(this.toString(), "filter() failed: " + System.nanoTime());
            if(query.indexOf('@') == -1 || query.indexOf('@') == query.length() - 1)
                return results;

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(new Uri.Builder().scheme("mailto").opaquePart(query).build());
            intent.putExtra(Module.RESPONSE_TEXT, "-Send email to " + query);
            results.add(intent);
        }

android.util.Log.i(this.toString(), "filter() completed: " + System.nanoTime());
        return results;
    }


    private Intent makeQuickActionIntent(String displayNamePrimary,
            String addr)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(new Uri.Builder().scheme("mailto").opaquePart(addr).build());
        intent.putExtra(Module.RESPONSE_TEXT,
                String.format("-Send email to %1$s (%2$s)",
                        displayNamePrimary,
                        addr));
        return intent;
    }
}
