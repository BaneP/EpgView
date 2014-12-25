package com.mole.epgview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by bane on 25/12/14.
 */
public class EpgView extends ViewGroup {
    public static final int INVALID_VALUE = -1;

    private int mChannelRowHeight = INVALID_VALUE;
    /**
     * Default is one minute = one pixel
     */
    private int mOneMinutePixelWidth = 1;
    private int mVerticalSpacing = 1;

    public EpgView(Context context) {
        super(context);
        init(context, null);
    }

    public EpgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EpgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.EpgView, 0, 0);
        try {
            mChannelRowHeight = a.getDimensionPixelSize(R.styleable.EpgView_channelRowHeight, INVALID_VALUE);
            mOneMinutePixelWidth = a.getDimensionPixelSize(R.styleable.EpgView_oneMinutePixelWidth, 1);
            mVerticalSpacing = a.getDimensionPixelSize(R.styleable.EpgView_verticalSpacing, 1);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int currentX = getPaddingLeft();
        int currentY = getPaddingTop();

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (currentX + child.getMeasuredWidth() > widthSize-getPaddingRight()) {
                currentY += mChannelRowHeight + mVerticalSpacing;
                currentX = getPaddingLeft();
            }

            lp.x=currentX+getMeasuredWidth();
            lp.y=currentY;
        }

        setMeasuredDimension(resolveSize(widthSize, widthMeasureSpec), resolveSize(heightSize, heightMeasureSpec));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        private int x, y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
