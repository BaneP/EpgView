package com.mole.epgview;

import java.util.ArrayDeque;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

	private int mTotalWidth;
	private int mCurrentScroll;

	private EpgAdapter mAdapter = null;

	private Recycler mRecycler;
	
	private Flinger mFlinger;
	
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
		mFlinger=new Flinger();
		// Calculate total grid width
		mTotalWidth = 5205;//mOneMinutePixelWidth * NUMBER_OF_MINUTES_IN_DAY* mNumberOfDaysToDisplayData;
		mRecycler = new Recycler();
	}

	private View prepareView(int eventWidth, int channelPosition,
			int eventPosition) {
		LayoutParams oldLp = null;
		View child = mRecycler.get(eventWidth);
		if (child != null) {
			oldLp = (LayoutParams) child.getLayoutParams();
		}
		child = mAdapter.getView(channelPosition, eventPosition, child, this);

		LayoutParams lp = (LayoutParams) child.getLayoutParams();
		// always use our LayoutParams
		if (lp == null || oldLp != lp) {
			lp = new LayoutParams(eventWidth, mChannelRowHeight);
			child.setLayoutParams(lp);

			// every child width must be exactly like its defined
			// (that is his duration)
			// and must have height like mChannelRowHeight
			child.measure(
					MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
		}// TODO check if this is ok to measure child only once
		return child;
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
			removeAllViewsInLayout();
			int currentX = getPaddingLeft();
			int currentY = getPaddingTop();

			if (mAdapter != null) {
				final int channelsCount = mChannelItemCount;
				for (int i = 0; i < channelsCount; i++) {// TODO use first
															// visible indexes
					final int eventCount = getEventsCount(i);

					int[] firstPosInfo = mAdapter
							.getPositionAndOffsetForScrollValue(mCurrentScroll,
									i);
					if (firstPosInfo[1] < 0 || firstPosInfo[0] < 0) {
						continue;
					}
//					Log.d(TAG, "onLayout firstPosInfo=" + firstPosInfo[0]
//							+ " - " + firstPosInfo[1]);
					currentX = currentX - firstPosInfo[1];
					for (int j = firstPosInfo[0]; j < eventCount; j++) {
						final int eventWidth = mAdapter.getEventWidth(i, j)
								- (j == 0 ? 0 : mHorizontalDivider);
						View child = prepareView(eventWidth, i, j);

						child.layout(currentX, currentY,
								currentX + child.getMeasuredWidth(), currentY
										+ child.getMeasuredHeight());
						addViewInLayout(child, -1, child.getLayoutParams(),
								true);
						// Log.d(TAG, "onLayout CHILD i=" + i + ", j=" + j);
						// Log.d(TAG,
						// "onLayout CHILD VIEW DIMENSIONS, w="
						// + child.getMeasuredWidth() + ", h="
						// + child.getMeasuredHeight());
						// Log.d(TAG, "onLayout CHILD POSITION, x=" + currentX
						// + ", y=" + currentY);
						// If child is out of screen
						if (currentX + child.getMeasuredWidth() > getWidth()
								- getPaddingRight()) {
							break;
						} else {
							currentX += child.getMeasuredWidth()
									+ mHorizontalDivider;
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
	float mTouchX=0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			mTouchX=0;
			return true;
		}
		case MotionEvent.ACTION_DOWN: {
			mTouchX=event.getX();
			return true;
		}
		case MotionEvent.ACTION_MOVE: {
			final int oldScroll=mCurrentScroll;
			mCurrentScroll=mCurrentScroll +(int) (mTouchX-event.getX());
			mTouchX=event.getX();
			if(mCurrentScroll<0){
				mCurrentScroll=0;
			}
			if(mCurrentScroll+getWidth()>mTotalWidth){
				mCurrentScroll=mTotalWidth-getWidth();
			}
			Log.d(TAG, "MOVED BY "+(mTouchX-event.getX()));
			Log.d(TAG, "mCurrentScroll="+mCurrentScroll);
			
			
			layoutChildren();
			invalidate();
			return true;
		}
		default:
			break;
		}
		return super.onTouchEvent(event);
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
		mSelectedChannelPosition = INVALID_POSITION;
		mSelectedEventPosition = INVALID_POSITION;
		mFirstChannelPosition = 0;
		mFirstEventsPositions = new ArrayList<Integer>();
		mRecycler.recycleAll();
		mRecycler.initialize();
		
		mCurrentScroll = 0;
		requestLayout();
	}

	@Override
	public View getSelectedView() {
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

		void initialize() {
			mRecycledViews = new SparseArray<ArrayDeque<View>>();
			mActiveViews = new ArrayList<View>();
		}

		void add(View view) {
			final int viewWidth = view.getWidth();
			ArrayDeque<View> list = mRecycledViews.get(viewWidth);
			if (list == null) {
				list = new ArrayDeque<View>();
				mRecycledViews.put(viewWidth, list);
			}
			list.offer(view);
		}

		View get(final int viewWidth) {
			ArrayDeque<View> list = mRecycledViews.get(viewWidth);
			if (list != null) {
				return list.poll();
			}
			return null;
		}

		void recycleAll() {
			final int size = mRecycledViews.size();
			for (int i = 0; i < size; i++) {
				mRecycledViews.get(mRecycledViews.keyAt(i)).clear();
			}
			mRecycledViews.clear();
			removeAllViewsInLayout();
		}
	}
	
	private class Flinger implements Runnable {
	    private final Scroller scroller;

	    private int lastX = 0;

	    Flinger() {
	        scroller = new Scroller(EpgView.this.getContext());
	    }

	    void start(int initialVelocity) {
	        int initialX = EpgView.this.getScrollX();
	        int maxX = Integer.MAX_VALUE; // or some appropriate max value in your code
	        scroller.fling(initialX, 0, initialVelocity, 0, 0, maxX, 0, 10);
	        Log.i(TAG, "starting fling at " + initialX + ", velocity is " + initialVelocity + "");

	        lastX = initialX;
	        EpgView.this.post(this);
	    }

	    public void run() {
	        if (scroller.isFinished()) {
	            Log.i(TAG, "scroller is finished, done with fling");
	            return;
	        }

	        boolean more = scroller.computeScrollOffset();
	        int x = scroller.getCurrX();
	        int diff = lastX - x;
	        if (diff != 0) {
	            EpgView.this.scrollBy(diff, 0);
	            lastX = x;
	        }

	        if (more) {
	        	EpgView.this.post(this);
	        }
	    }

	    boolean isFlinging() {
	        return !scroller.isFinished();
	    }

	    void forceFinished() {
	        if (!scroller.isFinished()) {
	            scroller.forceFinished(true);
	        }
	    }
	}
}
