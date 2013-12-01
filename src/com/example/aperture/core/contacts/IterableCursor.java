package com.example.aperture.core.contacts;

import android.database.Cursor;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class IterableCursor implements Iterator<Cursor>
{
    private Cursor mData;

    public IterableCursor(Cursor data)
    {
        mData = null;

        if(data == null) return;
        if(data.getCount() == 0)
        {
            data.close();
            return;
        }

        mData = data;
    }

    @Override
    public boolean hasNext()
    {
        if(mData == null) return false;
        if(mData.isLast())
        {
            mData.close();
            mData = null;
            return false;
        }
        return true;
    }

    @Override
    public Cursor next()
    {
        if(mData == null) throw new NoSuchElementException();
        if(mData.isLast())
        {
            mData.close();
            mData = null;
            throw new NoSuchElementException();
        }

        if(mData.isBeforeFirst()) mData.moveToFirst();
        else mData.moveToNext();
        return mData;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
