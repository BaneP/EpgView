package com.mole.epgview;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class EpgAdapter implements IEpgAdapter {
	private final DataSetObservable mDataSetObservable = new DataSetObservable();

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.unregisterObserver(observer);
	}

	/**
	 * Notifies the attached observers that the underlying data has been changed
	 * and any View reflecting the data set should refresh itself.
	 */
	public void notifyDataSetChanged() {
		mDataSetObservable.notifyChanged();
	}

	/**
	 * Notifies the attached observers that the underlying data is no longer
	 * valid or available. Once invoked this adapter is no longer valid and
	 * should not report further data set changes.
	 */
	public void notifyDataSetInvalidated() {
		mDataSetObservable.notifyInvalidated();
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 0;
	}

	@Override
	public boolean isEmpty(int channel) {
		return getEventsCount(channel) == 0;
	}

	@Override
	public boolean isEmpty() {
		return getChannelsCount() == 0;
	}

}
