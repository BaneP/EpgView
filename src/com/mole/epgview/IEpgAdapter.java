package com.mole.epgview;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public interface IEpgAdapter {
	/**
	 * Register an observer that is called when changes happen to the data used
	 * by this adapter.
	 * 
	 * @param observer
	 *            the object that gets notified when the data set changes.
	 */
	void registerDataSetObserver(DataSetObserver observer);

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver}.
	 * 
	 * @param observer
	 *            the object to unregister.
	 */
	void unregisterDataSetObserver(DataSetObserver observer);

	/**
	 * How many items are in the data set represented by this Adapter.
	 * 
	 * @return Count of items.
	 */
	int getChannelsCount();

	/**
	 * How many event items are in the channel data set.
	 * 
	 * @param channel
	 *            index for events
	 * 
	 * @return Count of events.
	 */
	int getEventsCount(int channel);

	/**
	 * Return width for desired channel event
	 * 
	 * @param channel
	 *            index
	 * @param event
	 *            index
	 * @return Event width in minutes
	 */
	int getEventWidth(int channel, int event);
	
	/**
	 * If event with defined channel index and event index contains regular data.
	 * @param channel Index of channel.
	 * @param event index of desired event.
	 * @return True if event is regular data object, False if it is dummy space.
	 */
	boolean hasRegularData(int channel, int event);
	
	
	/**
	 * Get information about first position for desired channel
	 * @param scroll Current X scroll
	 * @param channel Desired channel
	 * @return [0] - Index of first child, [1] child's invisible part of view
	 */
	int[] getPositionAndOffsetForScrollValue(int scroll,int channel);

	/**
	 * Get the data item associated with the specified position in the data set.
	 * 
	 * @param channel
	 *            Position of the item whose data we want within the adapter's
	 *            data set.
	 * @return The data at the specified position.
	 */
	Object getItem(int channel);

	/**
	 * Get the data item associated with the specified position in the data set.
	 * 
	 * @param channel
	 *            Position of the item whose data we want within the adapter's
	 *            data set.
	 * @param event
	 *            index for desired channel
	 * @return The data at the specified position.
	 */
	Object getItem(int channel, int event);

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set. You can either create a View manually or inflate it from an XML
	 * layout file. When the View is inflated, the parent View will apply layout
	 * parameters using {@link IEpgAdapter#getEventWidth(int, int)} unless you
	 * use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param channel
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param event
	 *            The position of event for desired channel.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 *            Heterogeneous lists can specify their number of view types, so
	 *            that this View is always of the right type (see
	 *            {@link #getViewTypeCount()} and {@link #getItemViewType(int)}
	 *            ).
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	View getView(int channel, int event, View convertView, ViewGroup parent);

	/**
	 * Get the type of View that will be created by {@link #getView} for the
	 * specified item.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set whose
	 *            view type we want.
	 * @return An integer representing the type of View. Two views should share
	 *         the same type if one can be converted to the other in
	 *         {@link #getView}. Note: Integers must be in the range 0 to
	 *         {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
	 *         also be returned.
	 * @see #IGNORE_ITEM_VIEW_TYPE
	 */
	int getItemViewType(int position);

	/**
	 * <p>
	 * Returns the number of types of Views that will be created by
	 * {@link #getView}. Each type represents a set of views that can be
	 * converted in {@link #getView}. If the adapter always returns the same
	 * type of View for all items, this method should return 1.
	 * </p>
	 * <p>
	 * This method will only be called when when the adapter is set on the the
	 * {@link AdapterView}.
	 * </p>
	 * 
	 * @return The number of types of Views that will be created by this adapter
	 */
	int getViewTypeCount();

	/**
	 * @return true if this adapter doesn't contain any data. This is used to
	 *         determine whether the empty view should be displayed. A typical
	 *         implementation will return getCount() == 0 but since getCount()
	 *         includes the headers and footers, specialized adapters might want
	 *         a different behavior.
	 */
	boolean isEmpty();
	
	/**
	 * @return true if this adapter doesn't contain any data. This is used to
	 *         determine whether the empty view should be displayed. A typical
	 *         implementation will return getCount() == 0 but since getCount()
	 *         includes the headers and footers, specialized adapters might want
	 *         a different behavior.
	 */
	boolean isEmpty(int channel);
}
