package com.ablingbling.library.nicespinner;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressWarnings("unused")
public abstract class NiceSpinnerBaseAdapter<T> extends BaseAdapter {

    private final SpinnerTextFormatter mSpinnerTextFormatter;

    private int mTextColor;
    private int mBackgroundSelector;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    protected int mSelectedIndex;

    NiceSpinnerBaseAdapter(Context context, int textColor, int backgroundSelector, SpinnerTextFormatter spinnerTextFormatter, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        mSpinnerTextFormatter = spinnerTextFormatter;
        mBackgroundSelector = backgroundSelector;
        mTextColor = textColor;
        mPaddingLeft = paddingLeft;
        mPaddingTop = paddingTop;
        mPaddingRight = paddingRight;
        mPaddingBottom = paddingBottom;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        TextView textView;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.spinner_list_item, null);
            textView = convertView.findViewById(R.id.text_view_spinner);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                textView.setBackground(ContextCompat.getDrawable(context, mBackgroundSelector));
            }

            convertView.setTag(new ViewHolder(textView));

        } else {
            textView = ((ViewHolder) convertView.getTag()).textView;
        }

        textView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        textView.setText(mSpinnerTextFormatter.format(getItem(position).toString()));
        textView.setTextColor(mTextColor);

        return convertView;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    public abstract T getItemInDataset(int position);

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract T getItem(int position);

    @Override
    public abstract int getCount();

    static class ViewHolder {
        TextView textView;

        ViewHolder(TextView textView) {
            this.textView = textView;
        }
    }

}