package com.example.admin.savefiledemo.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.savefiledemo.R;

/**
 * Created by carson2440 on 2016/7/4.
 */
public class RatioLayout extends ViewGroup {

    private float heightWidthRatio = 0.325f;

    public RatioLayout(Context context) {
        this(context, null);
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.RatioLayout);
        heightWidthRatio = getFloatFromString(a.getString(R.styleable.RatioLayout_height_width_ratio));
        a.recycle();
    }

    public void setHeightWidthRatio(String ratio) {
        heightWidthRatio = getFloatFromString(ratio);
    }

    public static float getFloatFromString(String src) {
        if (TextUtils.isEmpty(src)) {
            return 0;
        }
        float result;
        try {
            result = Float.parseFloat(src);
            return result;
        } catch (Exception e) {
        }

        String[] strs = src.split("/");
        if (strs.length == 2) {
            try {
                float molecular = Float.parseFloat(strs[0]);//分子
                float denominator = Float.parseFloat(strs[1]);//分子
                result = molecular / denominator;
            } catch (Exception e) {
                result = 0;
            }
        } else {
            result = 0;
        }
        return result;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom);
    }

    void layoutChildren(int left, int top, int right, int bottom) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = child.getLayoutParams();
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();
                child.layout(0, 0, width, 0 + height);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (heightWidthRatio > 0) {
            int width = getMeasuredWidth();
            int height = (int) (width * heightWidthRatio);
            setMeasuredDimension(width, height);
            int count = getChildCount();
            if (count >= 1) {
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    child.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
                }
            }
        }
    }
}
