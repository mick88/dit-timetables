package com.mick88.dittimetable.list;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.utils.FontApplicator;

public class EventAdapter extends ArrayAdapter<EventItem>
{	
	/**
	 * Interface for event list item. Represents any of the following:
	 * - Event / lecture
	 * - a group of events at the same time
	 * - one or more hour break between events
	 */
	public static interface EventItem
	{
		public static final int 
			ITEM_TYPE_EVENT = 0,
			ITEM_TYPE_MULTIEVENT = 1,
			ITEM_TYPE_SEPARATOR = 2,
			NUM_TYPES = 3;
		
		int getViewType();
		View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent);
	}
	
	
	
	FontApplicator fontApplicator;

	public EventAdapter(Context context, List<EventItem> objects) 
	{
		super(context, R.layout.timetable_event_small, objects);
		fontApplicator = new FontApplicator(getContext().getAssets(), "Roboto-Light.ttf");
	}
	
	LayoutInflater getLayoutInflater()
	{
		return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return getItem(position).getView(getLayoutInflater(), convertView, parent);
		/*View view = convertView;
	
		switch (getItemViewType(position))
		{
			case EventItem.ITEM_TYPE_EVENT:
				final EventViewHolder viewHolder;
				final TimetableEvent event = (TimetableEvent) getItem(position);
				
				if (view == null)
				{
					view = getItem(position).getView(getLayoutInflater(), convertView, parent);
					fontApplicator.applyFont(view);
					viewHolder = new EventViewHolder(view);

					view.setTag(viewHolder);
				}
				else viewHolder = (EventViewHolder) view.getTag();
				
				if (event == null) return view;
				
				
				break;
				
			default:
				return getItem(position).getView(getLayoutInflater(), null, parent);
		}
		return view;*/
	}
	
	@Override
	public int getItemViewType(int position)
	{
		return getItem(position).getViewType();
	}
	
	@Override
	public int getViewTypeCount()
	{
		return EventItem.NUM_TYPES;
	}
}
