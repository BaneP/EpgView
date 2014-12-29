package com.mole.epgview;

import java.util.ArrayDeque;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;
import android.widget.Scroller;

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
	 * Divider pixel sizes
	 */
	private int mVerticalDivider = 1;
	private int mHorizontalDivider = 0;

	/**
	 * Calculated total width
	 */
	private int mTotalWidth;
	private int mTotalHeight;
	// private int mCurrentScroll;

	protected int mCurrentOffsetX;
	protected int mCurrentOffsetY;

	private EpgAdapter mAdapter = null;

	private Recycler mRecycler;

	/**
	 * Scroller
	 */
	private OverScroller mScroll;

	/**
	 * View that is touched
	 */
	private View mTouchedView;
	/**
	 * For handling gestures events
	 */
	private GestureDetector mGestureDetector;
	private final GestureDetector.SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			offsetBy(distanceX, distanceY);
			return true;
		};

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			fling((int) velocityX, (int) velocityY);
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
		};

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mTouchedView == null)
				return true;

			// handleItemTap(mTouchedView);
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
		} finally {
			a.recycle();
		}
		mGestureDetector = new GestureDetector(getContext(), mGestureListener);
		mScroll = new OverScroller(context);
		// Calculate total grid width
		// TODO change to calculated
		mTotalWidth = 5205;// mOneMinutePixelWidth * NUMBER_OF_MINUTES_IN_DAY*
							// mNumberOfDaysToDisplayData;
		mRecycler = new Recycler();
		setFocusable(true);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		super.onTouchEvent(event);
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();

		// offset content if the scroller fling is still active
		if (mScroll.computeScrollOffset()) {
			offsetBy(mCurrentOffsetX + mScroll.getCurrX(), mCurrentOffsetY
					+ mScroll.getCurrY());
		}
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		return -mCurrentOffsetX;
	}

	@Override
	protected int computeVerticalScrollOffset() {
		return -mCurrentOffsetY;
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
		layoutChildren();
		mInLayout = false;
	}

	private void calculateFirstChannelPosition() {
		mFirstChannelPosition = mCurrentOffsetY / mChannelItemCount;
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
				calculateFirstChannelPosition();
				for (int i = mFirstChannelPosition; i < channelsCount; i++) {// TODO
																				// use
																				// first
					// visible indexes
					final int eventCount = getEventsCount(i);

					int[] firstPosInfo = mAdapter
							.getPositionAndOffsetForScrollValue(
									mCurrentOffsetX, i);
					Log.d(TAG, "layoutChildren");
					// No data for desired channel, don't draw anything
					if (firstPosInfo[1] < 0 || firstPosInfo[0] < 0) {
						continue;
					}
					currentX = currentX - firstPosInfo[1];
					for (int j = firstPosInfo[0]; j < eventCount; j++) {
						final int eventWidth = mAdapter.getEventWidth(i, j)
								- (j == 0 ? 0 : mHorizontalDivider);
						final boolean attached=isItemAttached(i, j);
						Log.d(TAG, "IS ITEM ATTACHED, i="+i+", j="+j+", res="+attached);
						if (!attached) {
							View child = mAdapter.getView(i, j,
									mRecycler.get(eventWidth), EpgView.this);
							addChildView(child, currentX, currentY, eventWidth,
									mChannelRowHeight);
						}
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
						break;
					} else {
						currentY += mChannelRowHeight + mVerticalDivider;
						currentX = getPaddingLeft();
					}
				}
			}
		} finally {
			if (!blockLayoutRequests) {
				mBlockLayoutRequests = false;
			}
		}
	}

	protected boolean isItemAttached(int channelIndex, int eventIndex) {
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
	 * Add new item and its view to the layout.
	 * 
	 * @param item
	 *            Item to add.
	 * @param left
	 *            Left position, relative to parent.
	 * @param top
	 *            Top position, relative to parent.
	 * @param width
	 *            Item view width.
	 * @param height
	 *            Item view height.
	 */
	private void addChildView(View child, int left, int top, int width,
			int height) {
		mRecycler.add(child);
		addViewToLayout(child, width, height, 0, 0);// TODO use real channel
													// index
		measureItemView(child, width, height);
		child.layout(left, top, left + width, top + height);
		child.invalidate();
	}

	private void measureItemView(final View view, final int width,
			final int height) {
		final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
				MeasureSpec.EXACTLY);
		final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
				MeasureSpec.EXACTLY);
		view.measure(widthMeasureSpec, heightMeasureSpec);
	}

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
	 * @param offsetDeltaX
	 *            Offset X delta.
	 * @param offsetDeltaY
	 *            Offset Y delta.
	 */
	private void offsetBy(float offsetDeltaX, float offsetDeltaY) {
		// adjust offset values
		if (offsetDeltaX + mCurrentOffsetX > mTotalWidth - getWidth()) {
			offsetDeltaX = mTotalWidth - getWidth() - mCurrentOffsetX;
		}
		if (offsetDeltaY + mCurrentOffsetY > mTotalHeight - getHeight()) {
			offsetDeltaY = mTotalHeight - getHeight() - mCurrentOffsetY;
		}

		// offset views
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).offsetLeftAndRight((int) offsetDeltaX);
			getChildAt(i).offsetTopAndBottom((int) offsetDeltaY);
		}

		// update state
		mCurrentOffsetX -= offsetDeltaX;
		mCurrentOffsetY -= offsetDeltaY;
		Log.d(TAG, "offsetBy NEW X="+mCurrentOffsetX+", NEW Y="+mCurrentOffsetY);
		update();
	}

	private void fling(int velocityX, int velocityY) {
		mScroll.forceFinished(true);
		mScroll.fling(-mCurrentOffsetX, -mCurrentOffsetY, velocityX, velocityY,
				0, mTotalWidth - getWidth(), 0, mTotalHeight - getHeight());
		invalidate();
	}

	private void removeInvisibleItems() {
		for (int i = mRecycler.mActiveViews.size() - 1; --i >= 0;) {
			View v = mRecycler.mActiveViews.get(i);
			if (isViewInvisible(v)) {
				mRecycler.mActiveViews.remove(i);
				removeItemFromLayout(v);
			}
		}
	}

	private void removeItemFromLayout(View child) {
		mRecycler.recycle(child);
		removeViewInLayout(child);
	}

	/**
	 * Check is view (at least one pixel of it) is inside the visible area or
	 * not.
	 * 
	 * @param view
	 *            View to check is it visible or not.
	 * @return true if the view is invisible i.e. out of visible screen bounds,
	 *         false - if at least one pixel of it is visible.
	 */
	private boolean isViewInvisible(View view) {
		return view == null || view.getLeft() >= getMeasuredWidth()
				|| view.getRight() <= 0 || view.getTop() >= getMeasuredHeight()
				|| view.getBottom() <= 0;
	}

	// private View prepareView(int eventWidth, int channelPosition,
	// int eventPosition) {
	// LayoutParams oldLp = null;
	// View child = mRecycler.get(eventWidth);
	// if (child != null) {
	// oldLp = (LayoutParams) child.getLayoutParams();
	// }
	// child = mAdapter.getView(channelPosition, eventPosition, child, this);
	//
	// LayoutParams lp = (LayoutParams) child.getLayoutParams();
	// // always use our LayoutParams
	// if (lp == null || oldLp != lp) {
	// lp = new LayoutParams(eventWidth, mChannelRowHeight);
	// child.setLayoutParams(lp);
	//
	// // every child width must be exactly like its defined
	// // (that is his duration)
	// // and must have height like mChannelRowHeight
	// child.measure(
	// MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
	// MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
	// }// TODO check if this is ok to measure child only once
	// mRecycler.add(child);
	// return child;
	// }

	// private Runnable mUpdateRunnable = new Runnable() {
	// @Override
	// public void run() {
	// mScrollIdle = true;
	// layoutChildren();
	// }
	// };

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
		mSelectedChannelPosition = INVALID_POSITION;
		mSelectedEventPosition = INVALID_POSITION;
		mFirstChannelPosition = 0;
		mFirstEventsPositions = new ArrayList<Integer>();
		mRecycler.clearAll();

		// mScroll.startScroll(0, 0, 0, 0, 0);
		// mScroll.abortAnimation();

		removeAllViewsInLayout();
		requestLayout();
		invalidate();
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	/**
	 * Find item that was touched.
	 * 
	 * @param touchX
	 *            X coordinate of touch.
	 * @param touchY
	 *            Y coordinate of touch.
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
