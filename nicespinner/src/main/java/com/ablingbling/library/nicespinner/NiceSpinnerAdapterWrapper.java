package com.ablingbling.library.nicespinner;

import android.content.Context;
import android.widget.ListAdapter;

public class NiceSpinnerAdapterWrapper extends NiceSpinnerBaseAdapter {

    private final ListAdapter baseAdapter;

    NiceSpinnerAdapterWrapper(Context context, ListAdapter toWrap, int textColor, int backgroundSelector, SpinnerTextFormatter spinnerTextFormatter, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        super(context, textColor, backgroundSelector, spinnerTextFormatter, paddingLeft, paddingTop, paddingRight, paddingBottom);
        baseAdapter = toWrap;
    }

    @Override
    public int getCount() {
        return baseAdapter.getCount() - 1;
    }

    @Override
    public Object getItem(int position) {
        return baseAdapter.getItem(position >= mSelectedIndex ? position + 1 : position);
    }

    @Override
    public Object getItemInDataset(int position) {
        return baseAdapter.getItem(position);
    }

}