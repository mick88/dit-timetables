package com.mick88.dittimetable.list;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
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
		View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent, FontApplicator fontApplicator, boolean allowHighlight, Timetable timetable);
	}
	
	private FontApplicator fontApplicator;
	private final boolean isToday;
	Timetable timetable;

	public EventAdapter(Context context, List<EventItem> objects, TimetableDay timetableDay, Timetable timetable) 
	{
		super(context, R.layout.timetable_event_small, objects);
		fontApplicator = new FontApplicator(getContext().getAssets(), "Roboto-Light.ttf");
		this.isToday = timetableDay.isToday();
		this.timetable = timetable;
	}
	
	LayoutInflater getLayoutInflater()
	{
		return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return getItem(position).getView(getLayoutInflater(), convertView, parent, fontApplicator, isToday, timetable);
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
