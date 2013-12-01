package com.example.aperture.core.contacts;

import android.content.Context;
import android.content.Intent;
import java.lang.ref.WeakReference;
import java.util.List;


public abstract class QuickActionFilter
{
    protected WeakReference<Context> mContext;
    public abstract List<Intent> filter(String query);
}
