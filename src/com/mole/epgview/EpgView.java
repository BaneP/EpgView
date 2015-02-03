package com.mole.epgview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bane on 25/12/14.
 */
public class EpgView extends EpgAdapterView<EpgAdapter> {
    private static final String TAG = "EpgView";
    public static final int NUMBER_OF_MINUTES_IN_DAY = 1440;

    private int mChannelRowHeight = INVALID_POSITION;
    /**
     * Default is one minute = one pixel
     */
    private int mOneMinutePixelWidth = 1;
    /**
     * Number of days displayed in grid
     */
    private int mNumberOfDaysToDisplayData = 1;
    /**
     * Horizontal and vertical dividers pixel sizes
     */
    private int mVerticalDivider = 1;
    private int mHorizontalDivider = 0;

    /**
     * Calculated total width and height
     * (Represents total presentation area size)
     */
    private int mTotalWidth;
    private int mTotalHeight;
    /**
     * Current X/Y offset values (current scroll value)
     */
    protected int mCurrentOffsetX;
    protected int mCurrentOffsetY;

    /**
     * Epg data adapter
     */
    private EpgAdapter mAdapter = null;

    /**
     * Helper class for recycling unused views
     */
    private Recycler mRecycler;

    /**
     * Object for that helps with scrolling
     */
    private OverScroller mScroll;

    /**
     * Selection rectangle position object, holds information about selected view coordinates on screen
     */
    private Rect mSelectorRect = new Rect();
    /**
     * Selector drawable
     */
    private Drawable mSelector;

    /**
     * View that is first touched by user (used for user click)
     */
    private View mTouchedView;
    /**
     * Objects that helps with gesture events
     */
    private GestureDetector mGestureDetector;
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            offsetBy(distanceX, distanceY);
            selectNextView(mSelectedView,null);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }

        public boolean onDown(MotionEvent e) {
            // if touch down during fling animation - reset touched item so it
            // wouldn't be handled as item tap
            mTouchedView = mScroll.computeScrollOffset() ? null
                    : getTouchedView(e.getX(), e.getY());

            mScroll.forceFinished(true);
            postInvalidateOnAnimation();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mTouchedView == null) {
                return true;
            }
            LayoutParams lp = (LayoutParams) mTouchedView.getLayoutParams();
            if (lp != null) {
                performItemClick(mTouchedView, lp.mChannelIndex, lp.mEventIndex);
            }
            selectNextView(mSelectedView,mTouchedView);
            mTouchedView = null;
            return true;
        }
    };

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

    /**
     * Initialize all fields for EpgView
     */
    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.EpgView, 0, 0);
        try {
            mChannelRowHeight = a.getDimensionPixelSize(
                    R.styleable.EpgView_channelRowHeight, INVALID_POSITION);
            mOneMinutePixelWidth = a.getDimensionPixelSize(
                    R.styleable.EpgView_oneMinutePixelWidth, 1);
            mVerticalDivider = a.getDimensionPixelSize(
                    R.styleable.EpgView_verticalDivider, 1);
            mHorizontalDivider = a.getDimensionPixelSize(
                    R.styleable.EpgView_horizontalDivider, 0);
            mNumberOfDaysToDisplayData = a.getInt(
                    R.styleable.EpgView_numberOfDaysToDisplay, 1);
            mSelector = a.getDrawable(R.styleable.EpgView_selector);
        } finally {
            a.recycle();
        }
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        mScroll = new OverScroller(context, new DecelerateInterpolator());
        // Calculate total grid width
        // TODO change to calculated
        mTotalWidth = 5205;// mOneMinutePixelWidth * NUMBER_OF_MINUTES_IN_DAY*
        // mNumberOfDaysToDisplayData;
        mRecycler = new Recycler();
        setFocusable(true);
    }



    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        //		super.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        // offset content if the scroller fling is still active
        if (mScroll.computeScrollOffset()) {
            offsetBy(mScroll.getCurrX() - mCurrentOffsetX, mScroll.getCurrY()
                    - mCurrentOffsetY);
        }
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return mCurrentOffsetX;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return mCurrentOffsetY;
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return mTotalWidth;
    }

    @Override
    protected int computeVerticalScrollRange() {
        return mTotalHeight;
    }

    @Override
    protected void onAttachedToWindow() {
        // TODO
        // if (mAdapter != null && !mAdapterRegistered) {
        // mAdapterRegistered = true;
        // mAdapter.registerDataSetObserver(mDataSetObserver);
        // }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        // TODO
        // if (mAdapterRegistered) {
        // mAdapterRegistered = false;
        // mAdapter.unregisterDataSetObserver(mDataSetObserver);
        // }
        super.onDetachedFromWindow();
    }

    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests && !mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        if (!changed) {
            return;
        }
        //Layout children only if epg view changed its bounds
        layoutChildren();
        mInLayout = false;
    }

    /**
     * Recalculates first channel position
     */
    private void calculateFirstChannelPosition() {
        mFirstChannelPosition = mCurrentOffsetY
                / (mChannelRowHeight + mVerticalDivider);
    }

    /**
     * Draws children on the screen
     */
    private void layoutChildren() {

        final boolean blockLayoutRequests = mBlockLayoutRequests;
        if (blockLayoutRequests) {
            return;
        }

        mBlockLayoutRequests = true;
        try {
            int currentX = getPaddingLeft();
            int currentY = getPaddingTop();

            if (mAdapter != null) {
                final int channelsCount = mChannelItemCount;
                final int previousFirstChannelPosition = mFirstChannelPosition;
                mLastChannelPosition = INVALID_POSITION;
                calculateFirstChannelPosition();

                // View is scrolled vertically so we must update first events
                // positions map
                //                if (previousFirstChannelPosition != mFirstChannelPosition) {
                //                    final int length = mFirstEventsPositions.size();
                //                    int key = 0;
                //                    for (int i = 0; i < length; i++) {
                //                        key = mFirstEventsPositions.keyAt(i);
                //                    }
                //                    // TODO
                //                }
                Log.d(TAG, "layoutChildren mFirstChannelPosition="
                        + mFirstChannelPosition);
                // Calculate first child top invisible part
                currentY -= mCurrentOffsetY
                        % (mChannelRowHeight + mVerticalDivider);
                for (int i = mFirstChannelPosition; i < channelsCount; i++) {
                    // Get number of events
                    final int eventCount = getEventsCount(i);
                    //Obtain first event position for desired channel
                    //                    FirstEventPosition firstEventPosition = mFirstEventsPositions.get(i);
                    // Get first child position based on current scroll value
                    // and calculate its invisible part
                    int[] firstPosInfo = getPositionAndOffsetForScrollValue(
                            mCurrentOffsetX, i);
                    // No data for desired channel, don't draw anything
                    if (firstPosInfo[1] < 0 || firstPosInfo[0] < 0) {
                        continue;
                    }
                    currentX -= firstPosInfo[1];
                    if (firstPosInfo[0] > 0) {
                        currentX += mHorizontalDivider;
                    }

                    for (int j = firstPosInfo[0]; j < eventCount; j++) {

                        final int eventWidth = mAdapter.getEventWidth(i, j)
                                - (j == 0 ? 0 : mHorizontalDivider);
                        final boolean attached = isItemAttachedToWindow(i, j);
                        if (!attached) {
                            View child = mAdapter.getView(i, j,
                                    mRecycler.get(eventWidth), EpgView.this);
                            addChildView(child, currentX, currentY, eventWidth,
                                    mChannelRowHeight, i, j);
                        }

                        //Update first event info for desired channel
                        //                        if (j == firstPosInfo[0]) {
                        //                            if (firstEventPosition == null) {
                        //                                firstEventPosition = new FirstEventPosition(j);
                        //                            }
                        //                            firstEventPosition.setUpdatedInLayout(true);
                        //                            mFirstEventsPositions.put(i,firstEventPosition);
                        //                        }
                        // If child right edge is larger or equals to right
                        // bound of EpgView
                        if (currentX + eventWidth >= getWidth()
                                - getPaddingRight()) {
                            break;
                        } else {
                            currentX += eventWidth + mHorizontalDivider;
                        }
                    }
                    // If child row is out of screen
                    if (currentY + mChannelRowHeight + mVerticalDivider > getHeight()
                            - getPaddingBottom()) {
                        mLastChannelPosition = i;
                        break;
                    } else {
                        currentY += mChannelRowHeight + mVerticalDivider;
                        currentX = getPaddingLeft();
                    }
                }
                if (mLastChannelPosition == INVALID_POSITION) {
                    mLastChannelPosition = channelsCount - 1;
                }
            }
        } finally {
            if (!blockLayoutRequests) {
                mBlockLayoutRequests = false;
            }
        }
    }

    /**
     * Get information about first position for desired channel
     *
     * @param scroll  Current X scroll
     * @param channel Desired channel
     * @return [0] - Index of first child, [1] child's invisible part of view
     */
    private int[] getPositionAndOffsetForScrollValue(int scroll, int channel) {
        int[] retVal = { -1, -1 };
        int sum = 0;
        final int count = mAdapter.getEventsCount(channel);
        int width = 0;
        for (int i = 0; i < count; i++) {
            width = mAdapter.getEventWidth(channel, i);
            sum += width;
            if (sum > scroll) {
                retVal[0] = i;
                retVal[1] = width - (sum - scroll);
                return retVal;
            }
        }
        return retVal;
    }

    /**
     * Determine if view for desired channel and desired event is already visible
     *
     * @param channelIndex Desired channel index
     * @param eventIndex   Desired event index
     * @return TRUE if view is on the screen, FALSE otherwise
     */
    protected boolean isItemAttachedToWindow(int channelIndex, int eventIndex) {
        for (View v : mRecycler.mActiveViews) {
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            if (lp.mChannelIndex == channelIndex
                    && lp.mEventIndex == eventIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add new view to the layout.
     *
     * @param child        View to add.
     * @param left         Left position, relative to parent.
     * @param top          Top position, relative to parent.
     * @param width        Item view width.
     * @param height       Item view height.
     * @param channelIndex Channel index of view that is represent
     * @param eventIndex   Index of event this view represents
     */
    private void addChildView(View child, int left, int top, int width,
            int height, int channelIndex, int eventIndex) {
        mRecycler.add(child);
        addViewToLayout(child, width, height, channelIndex, eventIndex);
        measureItemView(child, width, height);
        child.layout(left, top, left + width, top + height);
        child.invalidate();
    }

    /**
     * Measures view with desired width and height
     *
     * @param view   View to measure
     * @param width  Desired width
     * @param height Desired height
     */
    private void measureItemView(final View view, final int width,
            final int height) {
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);
        view.measure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Adds created view to layout to be drawn
     *
     * @param view         View to add
     * @param width        Desired width of view
     * @param height       Desired height of view
     * @param channelIndex Channel index of view that is represent
     * @param eventIndex   Index of event this view represents
     */
    private void addViewToLayout(final View view, int width, int height,
            int channelIndex, int eventIndex) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(width, height);
        } else {
            params.width = width;
            params.height = height;
        }
        params.mChannelIndex = channelIndex;
        params.mEventIndex = eventIndex;
        addViewInLayout(view, -1, params, true);
    }

    /**
     * Updates screen while selection is moving or scrolling
     */
    private void update() {
        removeInvisibleItems();
        awakenScrollBars();
        layoutChildren();
        invalidate();
    }

    /**
     * Offset all children views, so user will see an illusion of content
     * scrolling or flinging.
     *
     * @param offsetDeltaX Offset X delta.
     * @param offsetDeltaY Offset Y delta.
     */
    private void offsetBy(float offsetDeltaX, float offsetDeltaY) {
        // adjust offset values
        final int adjustedOffsetDeltaX = adjustOffsetDelta(mCurrentOffsetX,
                offsetDeltaX, getRightOffsetBounds());
        final int adjustedOffsetDeltaY = adjustOffsetDelta(mCurrentOffsetY,
                offsetDeltaY, getBottomOffsetBounds());

        // offset views
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).offsetLeftAndRight(-adjustedOffsetDeltaX);
            getChildAt(i).offsetTopAndBottom(-adjustedOffsetDeltaY);
        }

        // update state
        mCurrentOffsetX += adjustedOffsetDeltaX;
        mCurrentOffsetY += adjustedOffsetDeltaY;
        update();
    }

    /**
     * Starts fling scroll of EpgView
     *
     * @param velocityX
     * @param velocityY
     */
    private void fling(int velocityX, int velocityY) {
        mScroll.forceFinished(true);
        mScroll.fling(mCurrentOffsetX, mCurrentOffsetY, velocityX, velocityY,
                0, getRightOffsetBounds(), 0, getBottomOffsetBounds());
        invalidate();
    }

    /**
     * @return Returns maximum amount of scroll to the bottom (how much mCurrentOffsetY can be)
     */
    private int getBottomOffsetBounds() {
        return computeVerticalScrollRange() - getHeight() + getPaddingBottom()
                + getPaddingBottom();
    }

    /**
     * @return Returns maximum amount of scroll to the right (how much mCurrentOffsetX can be)
     */
    private int getRightOffsetBounds() {
        return computeHorizontalScrollRange() - getWidth() + getPaddingLeft()
                + getPaddingRight();
    }

    /**
     * Adjust requested offset delta to don't allow move content outside of the
     * content bounds.
     *
     * @param currentOffset    Current offset.
     * @param offsetDelta      Desired offset delta to apply.
     * @param maxAllowedOffset Max allowed offset to left. Usually it's difference between
     *                         content size and view size.
     * @return Adjusted offset delta.
     */
    private int adjustOffsetDelta(int currentOffset, float offsetDelta,
            int maxAllowedOffset) {
        // if view content size is smaller than the view size, offset is 0, i.e.
        // we can't offset the content
        if (maxAllowedOffset < 0) {
            return 0;
        }

        // limit offset for top and left edges
        if (currentOffset + offsetDelta <= 0) {
            return -currentOffset;
        }

        // limit offset for bottom and right edges
        if (currentOffset + offsetDelta >= maxAllowedOffset) {
            return maxAllowedOffset - currentOffset;
        }
        return (int) offsetDelta;
    }

    /**
     * Remove invisible views and recycle them for later use
     */
    private void removeInvisibleItems() {
        for (int i = mRecycler.mActiveViews.size() - 1; --i >= 0; ) {
            View v = mRecycler.mActiveViews.get(i);
            if (isViewInvisible(v)) {
                mRecycler.mActiveViews.remove(i);
                mRecycler.recycle(v);
                removeViewInLayout(v);
            }
        }
    }

    /**
     * Check is view (at least one pixel of it) is inside the visible area or
     * not.
     *
     * @param view View to check is it visible or not.
     * @return true if the view is invisible i.e. out of visible screen bounds,
     * false - if at least one pixel of it is visible.
     */
    private boolean isViewInvisible(View view) {
        return view == null || view.getLeft() >= getMeasuredWidth()
                || view.getRight() <= 0 || view.getTop() >= getMeasuredHeight()
                || view.getBottom() <= 0;
    }

    /**
     * Make new view selected.
     * @param oldSelectedView
     * @param newSelectedView
     */
    private void selectNextView(View oldSelectedView, View newSelectedView) {
        if(oldSelectedView==null && newSelectedView==null){
            return;
        }
        Rect oldViewRect=null, newViewRect;
        if (oldSelectedView != null) {
            oldSelectedView.setSelected(false);
            oldViewRect=new Rect();
            oldViewRect.left=(int)oldSelectedView.getX();
            oldViewRect.top=(int)oldSelectedView.getY();
            oldViewRect.right=oldViewRect.left+oldSelectedView.getWidth();
            oldViewRect.bottom=oldViewRect.top+oldSelectedView.getHeight();
        }
        mSelectedView = newSelectedView;
        if(oldViewRect!=null){
            invalidate(oldViewRect);
        }
        if(newSelectedView==null){
            fireOnSelected();
            return;
        }
        newSelectedView.setSelected(true);
        newViewRect=new Rect();
        newViewRect.left=(int)newSelectedView.getX();
        newViewRect.top=(int)newSelectedView.getY();
        newViewRect.right=newViewRect.left+newSelectedView.getWidth();
        newViewRect.bottom=newViewRect.top+newSelectedView.getHeight();
        invalidate(newViewRect);
        fireOnSelected();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAdapter == null || mAdapter.getChannelsCount() == 0) {
            return false;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_RIGHT: {
            return true;
        }
        case KeyEvent.KEYCODE_DPAD_LEFT: {
            int selectedEventIndex = getSelectedItemEventPosition();
            if (selectedEventIndex > 0) {
                View nextView = getTouchedView(mSelectedView.getX() - mHorizontalDivider - 1, mSelectedView.getY());
                if (nextView != null) {
                    selectNextView(mSelectedView,nextView);
                }
            }
            return true;
        }
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_CENTER: {
            LayoutParams lp = (LayoutParams) mSelectedView.getLayoutParams();
            performItemClick(mSelectedView, lp.getChannelIndex(), lp.getEventIndex());
            return true;
        }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mSelectedView == null) {
            return;
        }

        mSelectorRect.left = mSelectedView.getLeft();
        mSelectorRect.top = mSelectedView.getTop();
        mSelectorRect.right = mSelectedView.getRight();
        mSelectorRect.bottom = mSelectedView.getBottom();

        mSelector.setBounds(mSelectorRect);
        mSelector.draw(canvas);
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
        mCurrentOffsetX = 0;
        mCurrentOffsetY = 0;
        mChannelItemCount = mAdapter.getChannelsCount();
        mTotalHeight = mChannelItemCount * mChannelRowHeight + mVerticalDivider
                * (mChannelItemCount - 1);
        mSelectedView = null;
        mFirstChannelPosition = 0;
        mLastChannelPosition = INVALID_POSITION;
        mFirstEventsPositions = new HashMap<Integer, FirstEventPosition>();
        mRecycler.clearAll();

        removeAllViewsInLayout();
        requestLayout();
        invalidate();
    }

    @Override
    public View getSelectedView() {
        return mSelectedView;
    }

    /**
     * Find item that was touched.
     *
     * @param touchX X coordinate of touch.
     * @param touchY Y coordinate of touch.
     * @return Touched item or null if nothing was found.
     */
    private View getTouchedView(float touchX, float touchY) {
        for (View view : mRecycler.mActiveViews) {
            if (touchX > view.getLeft() && touchX < view.getRight()
                    && view.getTop() < touchY && view.getBottom() > touchY) {
                return view;
            }
        }
        return null;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(
            ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        private int mChannelIndex = INVALID_POSITION;
        private int mEventIndex = INVALID_POSITION;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public int getChannelIndex() {
            return mChannelIndex;
        }

        public int getEventIndex() {
            return mEventIndex;
        }
    }

    class Recycler {
        SparseArray<ArrayDeque<View>> mRecycledViews = new SparseArray<ArrayDeque<View>>();
        ArrayList<View> mActiveViews = new ArrayList<View>();

        void add(View view) {
            mActiveViews.add(view);
        }

        View get(final int viewWidth) {
            ArrayDeque<View> list = mRecycledViews.get(viewWidth);
            if (list != null) {
                return list.poll();
            }
            return null;
        }

        void recycle(View view) {
            final int viewWidth = view.getWidth();
            ArrayDeque<View> list = mRecycledViews.get(viewWidth);
            if (list == null) {
                list = new ArrayDeque<View>();
                mRecycledViews.put(viewWidth, list);
            }
            list.offer(view);
        }

        void moveAllViewsToRecycle() {
            for (View v : mActiveViews) {
                recycle(v);
            }
            mActiveViews.clear();
        }

        void clearAll() {
            mActiveViews.clear();
            final int length = mRecycledViews.size();
            // Make sure to clear all views
            for (int i = 0; i < length; i++) {
                ArrayDeque<View> list = mRecycledViews.get(mRecycledViews
                        .keyAt(i));
                list.clear();
            }
            mRecycledViews.clear();
        }
    }
}
