package com.mole.epgview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;

public class MyActivity extends Activity {
	private static final String TAG="MyActivity";
	private EpgView mEpgView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mEpgView = (EpgView) findViewById(R.id.epg);
		mEpgView.setAdapter(new Adapter());
		mEpgView.setOnItemClickListener(new EpgView.OnItemClickListener() {

			@Override
			public void onItemClick(EpgAdapterView<?> parent, View view,
					int channelPosition, int eventPosition) {
				Toast.makeText(MyActivity.this, "ITEM CLICKED "+channelPosition+" "+eventPosition, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private class Adapter extends EpgAdapter {
		int[] widths = { 150, 550, 240, 370, 425, 150, 550, 240, 370, 425, 150,
				550, 240, 370, 425 };
		HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();

		/**
		 * How many items are in the data set represented by this Adapter.
		 * 
		 * @return Count of items.
		 */
		@Override
		public int getChannelsCount() {
			return 15;
		}

		/**
		 * How many event items are in the channel data set.
		 * 
		 * @param channel
		 *            index for events
		 * @return Count of events.
		 */
		@Override
		public int getEventsCount(int channel) {
			return 15;
		}

		/**
		 * Return width for desired channel event
		 * 
		 * @param channel
		 *            index
		 * @param event
		 *            index
		 * @return Event width in minutes
		 */
		@Override
		public int getEventWidth(int channel, int event) {
			Random rand = new Random();
			// return widths[rand.nextInt(1000)%5];
			return widths[event];
		}

		/**
		 * If event with defined channel index and event index contains regular
		 * data.
		 * 
		 * @param channel
		 *            Index of channel.
		 * @param event
		 *            index of desired event.
		 * @return True if event is regular data object, False if it is dummy
		 *         space.
		 */
		@Override
		public boolean hasRegularData(int channel, int event) {
			return false;
		}

		/**
		 * Get the data item associated with the specified position in the data
		 * set.
		 * 
		 * @param channel
		 *            Position of the item whose data we want within the
		 *            adapter's data set.
		 * @return The data at the specified position.
		 */
		@Override
		public Object getItem(int channel) {
			return null;
		}

		/**
		 * Get the data item associated with the specified position in the data
		 * set.
		 * 
		 * @param channel
		 *            Position of the item whose data we want within the
		 *            adapter's data set.
		 * @param event
		 *            index for desired channel
		 * @return The data at the specified position.
		 */
		@Override
		public Object getItem(int channel, int event) {
			return null;
		}

		/**
		 * Get a View that displays the data at the specified position in the
		 * data set. You can either create a View manually or inflate it from an
		 * XML layout file. When the View is inflated, the parent View will
		 * apply layout parameters using
		 * {@link com.mole.epgview.IEpgAdapter#getEventWidth(int, int)} unless
		 * you use
		 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
		 * to specify a root view and to prevent attachment to the root.
		 * 
		 * @param channel
		 *            The position of the item within the adapter's data set of
		 *            the item whose view we want.
		 * @param event
		 *            The position of event for desired channel.
		 * @param convertView
		 *            The old view to reuse, if possible. Note: You should check
		 *            that this view is non-null and of an appropriate type
		 *            before using. If it is not possible to convert this view
		 *            to display the correct data, this method can create a new
		 *            view. Heterogeneous lists can specify their number of view
		 *            types, so that this View is always of the right type (see
		 *            {@link #getViewTypeCount()} and
		 *            {@link #getItemViewType(int)} ).
		 * @param parent
		 *            The parent that this view will eventually be attached to
		 * @return A View corresponding to the data at the specified position.
		 */
		@Override
		public View getView(int channel, int event, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = new TextView(parent.getContext());
				convertView.setBackgroundColor(0x99FF0000);
			}
			((TextView) convertView).setText("channel " + channel + ", event "
					+ event);

			return convertView;
		}

		@Override
		public int[] getPositionAndOffsetForScrollValue(int scroll, int channel) {
			int[] retVal = { -1, -1 };
			int sum = 0;
			for (int i = 0; i < widths.length; i++) {
				sum += widths[i];
				if (sum > scroll) {
					retVal[0] = i;
					retVal[1] = widths[i] - (sum - scroll);
					return retVal;
				}
			}
			return retVal;
		}
	}
}
