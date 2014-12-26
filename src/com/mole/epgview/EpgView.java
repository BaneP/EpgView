package com.mole.epgview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by bane on 25/12/14.
 */
public class EpgView extends EpgAdapterView<EpgAdapter> {
    private static final String TAG = "EpgView";
    public static final int INVALID_VALUE = -1;

    private int mChannelRowHeight = INVALID_VALUE;
    /**
     * Default is one minute = one pixel
     */
    private int mOneMinutePixelWidth = 1;
    private int mVerticalSpacing = 1;

    private EpgAdapter mAdapter = null;

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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int currentX = getPaddingLeft();
        int currentY = getPaddingTop();


/*
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            final int childHeight = child.getMeasuredHeight();
            final int childWidth = child.getMeasuredWidth();
            if (currentX + childWidth > widthSize - getPaddingRight()) {
                currentY += mChannelRowHeight + mVerticalSpacing;
                currentX = getPaddingLeft();
            }

            lp.x = currentX;
            lp.y = currentY;
            currentX += childWidth;
        }
*/      if(mAdapter!=null) {
            final int channelsCount = mChannelItemCount;
            for (int i = 0; i < channelsCount; i++) {//TODO use first visible indexes
                final int eventCount = getEventsCount(i);
                for (int j = 0; j < eventCount; j++) {
                    View child = prepareView(null, i, j);
                    LayoutParams lp = new LayoutParams(mAdapter.getEventWidth(i,j),mChannelRowHeight);
                    child.setLayoutParams(lp);
                    measureChild(child, MeasureSpec.makeMeasureSpec(lp.width,MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(lp.height,MeasureSpec.EXACTLY));

                    final int childHeight = child.getMeasuredHeight();
                    final int childWidth = child.getMeasuredWidth();
                    lp.x = currentX;
                    lp.y = currentY;
                    //If child is out of screen
                    if (currentX + childWidth > widthSize - getPaddingRight()) {
                        break;
                    }else{
                        currentX += childWidth;
                    }
                }
                //If child row is out of screen
                if(currentY+mChannelRowHeight + mVerticalSpacing>heightSize-getPaddingBottom()){
                    break;
                }else {
                    currentY += mChannelRowHeight + mVerticalSpacing;
                    currentX = getPaddingLeft();
                }
            }
        }
        setMeasuredDimension(resolveSize(widthSize, widthMeasureSpec), resolveSize(heightSize, heightMeasureSpec));
    }

    private View prepareView(View view,int channelPosition,int eventPosition){
        View preparedView=mAdapter.getView(channelPosition,eventPosition,view,this);
        return preparedView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
        }
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
        /**
         * Coordinates on screen
         */
        private int x, y;

        /**
         * View positions on screen
         */
        private int mChannelPosition, mEventPosition;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getEventPosition() {
            return mEventPosition;
        }

        public int getChannelPosition() {
            return mChannelPosition;
        }

        public void setChannelPosition(int mChannelPosition) {
            this.mChannelPosition = mChannelPosition;
        }

        public void setEventPosition(int mEventPosition) {
            this.mEventPosition = mEventPosition;
        }
    }

    @Override
    public EpgAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(EpgAdapter adapter) {
        if (adapter == null) {
            return;
        }
        mAdapter = adapter;
        mChannelItemCount = mAdapter.getChannelsCount();
        mSelectedChannelPosition=INVALID_POSITION;
        mSelectedEventPosition=INVALID_POSITION;

        requestLayout();
    }

    @Override
    public View getSelectedView() {
        return null;
    }
}
