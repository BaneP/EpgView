package com.mole.epgview;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by bane on 26/12/14.
 */
public abstract class EpgAdapterView<T extends EpgAdapter> extends ViewGroup {

    /**
     * Represents an invalid position. All valid positions are in the range 0 to 1 less than the
     * number of items in the current adapter.
     */
    public static final int INVALID_POSITION = -1;

    /**
     * The listener that receives notifications when an item is selected.
     */
    OnItemSelectedListener mOnItemSelectedListener;
    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;
    /**
     * Selected positions
     */
    int mSelectedChannelPosition = INVALID_POSITION;
    int mSelectedEventPosition = INVALID_POSITION;

    /**
     * The number of channel items in the current adapter.
     */
    int mChannelItemCount=0;

    /**
     * First visible channel position
     */
    int mFirstChannelPosition;
    
    ArrayList<Integer> mFirstEventsPositions;
    
    /**
     * When set to true, calls to requestLayout() will not propagate up the parent hierarchy.
     * This is used to layout the children during a layout pass.
     */
    boolean mBlockLayoutRequests = false;
    
    /**
     * Indicates that this view is currently being laid out.
     */
    boolean mInLayout = false;

    public EpgAdapterView(Context context) {
        super(context);
    }

    public EpgAdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EpgAdapterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface OnItemClickListener {
        void onItemClick(EpgAdapterView<?> parent, View view, int channelPosition, int eventPosition);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * Call the OnItemClickListener, if it is defined. Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @param view            The view within the AdapterView that was clicked.
     * @param channelPosition The vertical position of the view in the adapter.
     * @param eventPosition   The horizontal position of the view in the adapter.
     * @param id              The row id of the item that was clicked.
     * @return True if there was an assigned OnItemClickListener that was
     * called, false otherwise is returned.
     */
    public boolean performItemClick(View view, int channelPosition, int eventPosition) {
        if (mOnItemClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnItemClickListener.onItemClick(this, view, channelPosition, eventPosition);
            if (view != null) {
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
            return true;
        }
        return false;
    }

    /**
     * TODO Define specific listeners here
     */
    public interface OnItemSelectedListener {
        void onItemSelected(EpgAdapterView<?> parent, View view, int channelPosition, int eventPosition);

        void onNothingSelected(EpgAdapterView<?> parent);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    public abstract T getAdapter();

    public abstract void setAdapter(T adapter);

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child) {
        throw new UnsupportedOperationException("addView(View) is not supported in EpgAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     * @param index Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child, int index) {
        throw new UnsupportedOperationException("addView(View, int) is not supported in EpgAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child  Ignored.
     * @param params Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) "
                + "is not supported in EpgAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child  Ignored.
     * @param index  Ignored.
     * @param params Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void addView(View child, int index, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) "
                + "is not supported in EpgAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param child Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported in EpgAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @param index Ignored.
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in EpgAdapterView");
    }

    /**
     * This method is not supported and throws an UnsupportedOperationException when called.
     *
     * @throws UnsupportedOperationException Every time this method is invoked.
     */
    @Override
    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in EpgAdapterView");
    }

    public int getSelectedItemChannelPosition() {
        return mSelectedChannelPosition;
    }

    public int getSelectedItemEventPosition() {
        return mSelectedEventPosition;
    }

    public abstract View getSelectedView();

    /**
     * @return The data corresponding to the currently selected channel item, or
     * null if there is nothing selected.
     */
    public Object getSelectedChannelItem() {
        T adapter = getAdapter();
        final int selectedChannel = getSelectedItemChannelPosition();
        if (adapter != null && adapter.getChannelsCount() > 0 && selectedChannel >= 0) {
            return adapter.getItem(selectedChannel);
        } else {
            return null;
        }
    }

    /**
     * @return The data corresponding to the currently selected channel event item, or
     * null if there is nothing selected.
     */
    public Object getSelectedEventItem() {
        T adapter = getAdapter();
        final int selectedChannel = getSelectedItemChannelPosition();
        final int selectedEvent = getSelectedItemEventPosition();
        if (adapter != null && adapter.getChannelsCount() > 0 && selectedChannel >= 0
                && adapter.getEventsCount(selectedChannel) > 0 && selectedEvent >= 0) {
            return adapter.getItem(selectedChannel, selectedEvent);
        } else {
            return null;
        }
    }

    /**
     * @return The number of channel items owned by the EpgAdapter associated with this
     * EpgAdapterView. (This is the number of data items, which may be
     * larger than the number of visible views.)
     */
    public int getChannelsCount() {
        return mChannelItemCount;
    }

    /**
     * @return The number of channel items owned by the EpgAdapter associated with this
     * EpgAdapterView. (This is the number of data items, which may be
     * larger than the number of visible views.)
     */
    public int getEventsCount(int channelIndex) {
        return getAdapter().getEventsCount(channelIndex);
    }

    /**
     * Gets the data associated with the specified position in the list.
     *
     * @param position Which data to get
     * @return The data associated with the specified position in the list
     */
    public Object getChannelItemAtPosition(int position) {
        T adapter = getAdapter();
        if (adapter != null && adapter.getChannelsCount() > position && position >= 0) {
            return adapter.getItem(position);
        } else {
            return null;
        }
    }

    /**
     * Gets the data associated with the specified position in the list.
     *
     * @param position Which data to get
     * @return The data associated with the specified position in the list
     */
    public Object getEventItemAtPosition(int channelPosition, int eventPosition) {
        T adapter = getAdapter();
        if (adapter != null && adapter.getChannelsCount() > channelPosition && channelPosition >= 0
                && adapter.getEventsCount(channelPosition) > eventPosition && eventPosition >= 0) {
            return adapter.getItem(channelPosition, eventPosition);
        } else {
            return null;
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new RuntimeException("Don't call setOnClickListener for an AdapterView. "
                + "You probably want setOnItemClickListener instead");
    }

    private void fireOnSelected() {
        if (mOnItemSelectedListener == null) {
            return;
        }
        final int channelSelection = getSelectedItemChannelPosition();
        final int eventSelection = getSelectedItemEventPosition();
        if (channelSelection >= 0) {
            View v = getSelectedView();
            mOnItemSelectedListener.onItemSelected(this, v, channelSelection, eventSelection);
        } else {
            mOnItemSelectedListener.onNothingSelected(this);
        }
    }

    /**
     * Get the position within the adapter's data set for the view, where view is a an adapter item
     * or a descendant of an adapter item.
     *
     * @param view an adapter item, or a descendant of an adapter item. This must be visible in this
     *             AdapterView at the time of the call.
     * @return the position within the adapter's data set of the view, or {@link #INVALID_POSITION}
     * if the view does not correspond to a list item (or it is not currently visible).
     */
    public int getPositionForView(View view) {
        int[] selectedPosition = { INVALID_POSITION, INVALID_POSITION };
        View listItem = view;
        try {
            View v;
            while (!(v = (View) listItem.getParent()).equals(this)) {
                listItem = v;
            }
        } catch (ClassCastException e) {
            // We made it up to the window without find this list view
            return INVALID_POSITION;
        }
        // Search the children for the list item
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).equals(listItem)) {
                return mFirstChannelPosition + i;
            }
        }
        // Child not found!
        return INVALID_POSITION;
    }

}
