package com.ablingbling.library.nicespinner;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class NiceSpinner extends AppCompatTextView {

    private static final int MAX_LEVEL = 10000;
    private static final int DEFAULT_ELEVATION = 16;
    private static final String INSTANCE_STATE = "instance_state";
    private static final String SELECTED_INDEX = "selected_index";
    private static final String IS_POPUP_SHOWING = "is_popup_showing";
    private static final String IS_ARROW_HIDDEN = "is_arrow_hidden";
    private static final String ARROW_DRAWABLE_RES_ID = "arrow_drawable_res_id";
    public static final int VERTICAL_OFFSET = 1;

    private Drawable arrowDrawable;
    private PopupWindow popupWindow;
    private ListView listView;
    private NiceSpinnerBaseAdapter adapter;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    private int mTextColor;
    private int mListTextColor;
    private int mArrowDrawableTint;
    private boolean mIsArrowHidden;
    @DrawableRes
    private int mArrowDrawableResId;
    private int mDropDownListPaddingBottom;
    private int mTextBackgroundSelector;
    private int mListBackgroundSelector;
    private int mTextPaddingLeft;
    private int mTextPaddingTop;
    private int mTextPaddingRight;
    private int mTextPaddingBottom;

    private int mDisplayHeight;
    private int mParentVerticalOffset;
    private int mSelectedIndex;

    private SpinnerTextFormatter spinnerTextFormatter = new SimpleSpinnerTextFormatter();
    private SpinnerTextFormatter selectedTextFormatter = new SimpleSpinnerTextFormatter();

    public NiceSpinner(Context context) {
        super(context);
        initData(context, null, 0);
        initView(context);
    }

    public NiceSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context, attrs, 0);
        initView(context);
    }

    public NiceSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(SELECTED_INDEX, mSelectedIndex);
        bundle.putBoolean(IS_ARROW_HIDDEN, mIsArrowHidden);
        bundle.putInt(ARROW_DRAWABLE_RES_ID, mArrowDrawableResId);
        if (popupWindow != null) {
            bundle.putBoolean(IS_POPUP_SHOWING, popupWindow.isShowing());
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle bundle = (Bundle) savedState;
            mSelectedIndex = bundle.getInt(SELECTED_INDEX);

            if (adapter != null) {
                setTextInternal(adapter.getItemInDataset(mSelectedIndex).toString());
                adapter.setSelectedIndex(mSelectedIndex);
            }

            if (bundle.getBoolean(IS_POPUP_SHOWING)) {
                if (popupWindow != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            showDropDown();
                        }
                    });
                }
            }

            mIsArrowHidden = bundle.getBoolean(IS_ARROW_HIDDEN, false);
            mArrowDrawableResId = bundle.getInt(ARROW_DRAWABLE_RES_ID);
            savedState = bundle.getParcelable(INSTANCE_STATE);
        }
        super.onRestoreInstanceState(savedState);
    }

    private void initData(Context context, AttributeSet attrs, int defStyleAttr) {
        mTextColor = getDefaultTextColor(context);
        mListTextColor = getDefaultTextColor(context);
        mArrowDrawableTint = Integer.MAX_VALUE;
        mIsArrowHidden = false;
        mArrowDrawableResId = R.drawable.arrow;
        mDropDownListPaddingBottom = 0;
        mTextBackgroundSelector = R.drawable.selector;
        mListBackgroundSelector = R.drawable.selector;
        mTextPaddingLeft = dp2px(context, 20);
        mTextPaddingTop = dp2px(context, 12);
        mTextPaddingRight = dp2px(context, 20);
        mTextPaddingBottom = dp2px(context, 12);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NiceSpinner, defStyleAttr, 0);

            mTextColor = ta.getColor(R.styleable.NiceSpinner_ns_textColor, mTextColor);
            mListTextColor = ta.getColor(R.styleable.NiceSpinner_ns_listTextColor, mListTextColor);
            mArrowDrawableTint = ta.getColor(R.styleable.NiceSpinner_ns_arrowTint, mArrowDrawableTint);
            mIsArrowHidden = ta.getBoolean(R.styleable.NiceSpinner_ns_hideArrow, mIsArrowHidden);
            mArrowDrawableResId = ta.getResourceId(R.styleable.NiceSpinner_ns_arrowDrawable, mArrowDrawableResId);
            mDropDownListPaddingBottom = ta.getDimensionPixelSize(R.styleable.NiceSpinner_ns_dropDownListPaddingBottom, mDropDownListPaddingBottom);
            mTextBackgroundSelector = ta.getResourceId(R.styleable.NiceSpinner_ns_textBackgroundSelector, mTextBackgroundSelector);
            mListBackgroundSelector = ta.getResourceId(R.styleable.NiceSpinner_ns_listBackgroundSelector, mListBackgroundSelector);
            mTextPaddingLeft = ta.getDimensionPixelSize(R.styleable.NiceSpinner_ns_textPaddingLeft, mTextPaddingLeft);
            mTextPaddingTop = ta.getDimensionPixelSize(R.styleable.NiceSpinner_ns_textPaddingTop, mTextPaddingTop);
            mTextPaddingRight = ta.getDimensionPixelSize(R.styleable.NiceSpinner_ns_textPaddingRight, mTextPaddingRight);
            mTextPaddingBottom = ta.getDimensionPixelSize(R.styleable.NiceSpinner_ns_textPaddingBottom, mTextPaddingBottom);

            ta.recycle();
        }
    }

    private void initView(Context context) {
        setClickable(true);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        setBackgroundResource(mTextBackgroundSelector);
        setTextColor(mTextColor);

        listView = new ListView(context);
        listView.setId(getId());
        listView.setDivider(null);
        listView.setItemsCanFocus(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= mSelectedIndex && position < adapter.getCount()) {
                    position++;
                }

                mSelectedIndex = position;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(parent, view, position, id);
                }

                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(parent, view, position, id);
                }

                adapter.setSelectedIndex(position);
                setTextInternal(adapter.getItemInDataset(position).toString());
                dismissDropDown();
            }

        });

        popupWindow = new PopupWindow(context);
        popupWindow.setContentView(listView);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(DEFAULT_ELEVATION);
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.spinner_drawable));

        } else {
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.drop_down_shadow));
        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                if (!mIsArrowHidden) {
                    animateArrow(false);
                }
            }

        });

        measureDisplayHeight();
    }

    private void measureDisplayHeight() {
        mDisplayHeight = getContext().getResources().getDisplayMetrics().heightPixels;
    }

    private int getParentVerticalOffset() {
        if (mParentVerticalOffset > 0) {
            return mParentVerticalOffset;
        }

        int[] locationOnScreen = new int[2];
        getLocationOnScreen(locationOnScreen);
        return mParentVerticalOffset = locationOnScreen[VERTICAL_OFFSET];
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        arrowDrawable = initArrowDrawable(mArrowDrawableTint);
        setArrowDrawableOrHide(arrowDrawable);
    }

    private Drawable initArrowDrawable(int drawableTint) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), mArrowDrawableResId);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            if (drawableTint != Integer.MAX_VALUE && drawableTint != 0) {
                DrawableCompat.setTint(drawable, drawableTint);
            }
        }
        return drawable;
    }

    private void setArrowDrawableOrHide(Drawable drawable) {
        if (!mIsArrowHidden && drawable != null) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    private int getDefaultTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int defaultTextColor = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();
        return defaultTextColor;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setArrowDrawable(@DrawableRes @ColorRes int drawableId) {
        mArrowDrawableResId = drawableId;
        arrowDrawable = initArrowDrawable(R.drawable.arrow);
        setArrowDrawableOrHide(arrowDrawable);
    }

    public void setArrowDrawable(Drawable drawable) {
        arrowDrawable = drawable;
        setArrowDrawableOrHide(arrowDrawable);
    }

    public void setTextInternal(String text) {
        if (selectedTextFormatter != null) {
            setText(selectedTextFormatter.format(text));
        } else {
            setText(text);
        }
    }

    /**
     * Set the default spinner item using its index
     *
     * @param position the item's position
     */
    public void setSelectedIndex(int position) {
        if (adapter != null) {
            if (position >= 0 && position <= adapter.getCount()) {
                adapter.setSelectedIndex(position);
                mSelectedIndex = position;
                setTextInternal(adapter.getItemInDataset(position).toString());
            } else {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }
        }
    }

    public void addOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public <T> void attachDataSource(List<T> list) {
        adapter = new NiceSpinnerAdapter<>(getContext(), list, mListTextColor, mListBackgroundSelector, spinnerTextFormatter, mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        setAdapterInternal(adapter);
    }

    public void setAdapter(ListAdapter adapter) {
        this.adapter = new NiceSpinnerAdapterWrapper(getContext(), adapter, mListTextColor, mListBackgroundSelector, spinnerTextFormatter, mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
        setAdapterInternal(this.adapter);
    }

    private void setAdapterInternal(NiceSpinnerBaseAdapter adapter) {
        mSelectedIndex = 0;
        listView.setAdapter(adapter);
        setTextInternal(adapter.getItemInDataset(mSelectedIndex).toString());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled() && event.getAction() == MotionEvent.ACTION_UP) {
            if (!popupWindow.isShowing()) {
                showDropDown();
            } else {
                dismissDropDown();
            }
        }
        return super.onTouchEvent(event);
    }

    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp ? 0 : MAX_LEVEL;
        int end = shouldRotateUp ? MAX_LEVEL : 0;
        ObjectAnimator animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
    }

    public void dismissDropDown() {
        if (!mIsArrowHidden) {
            animateArrow(false);
        }
        popupWindow.dismiss();
    }

    public void showDropDown() {
        if (!mIsArrowHidden) {
            animateArrow(true);
        }
        measurePopUpDimension();
        popupWindow.showAsDropDown(this);
    }

    private void measurePopUpDimension() {
        int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(mDisplayHeight - getParentVerticalOffset() - getMeasuredHeight(), MeasureSpec.AT_MOST);
        listView.measure(widthSpec, heightSpec);
        popupWindow.setWidth(listView.getMeasuredWidth());
        popupWindow.setHeight(listView.getMeasuredHeight() - mDropDownListPaddingBottom);
    }

    /**
     * 根据手机的分辨率从dp 的单位 转成为px(像素)
     */
    private int dp2px(Context context, float dpValue) {
        float density = context.getResources().getSystem().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public void setTintColor(@ColorRes int resId) {
        if (arrowDrawable != null && !mIsArrowHidden) {
            DrawableCompat.setTint(arrowDrawable, ContextCompat.getColor(getContext(), resId));
        }
    }

    public void setArrowTintColor(int resolvedColor) {
        if (arrowDrawable != null && !mIsArrowHidden) {
            DrawableCompat.setTint(arrowDrawable, resolvedColor);
        }
    }

    public void hideArrow() {
        mIsArrowHidden = true;
        setArrowDrawableOrHide(arrowDrawable);
    }

    public void showArrow() {
        mIsArrowHidden = false;
        setArrowDrawableOrHide(arrowDrawable);
    }

    public boolean isArrowHidden() {
        return mIsArrowHidden;
    }

    public void setDropDownListPaddingBottom(int paddingBottom) {
        mDropDownListPaddingBottom = paddingBottom;
    }

    public int getDropDownListPaddingBottom() {
        return mDropDownListPaddingBottom;
    }

    public void setSpinnerTextFormatter(SpinnerTextFormatter spinnerTextFormatter) {
        this.spinnerTextFormatter = spinnerTextFormatter;
    }

    public void setSelectedTextFormatter(SpinnerTextFormatter textFormatter) {
        this.selectedTextFormatter = textFormatter;
    }

}
