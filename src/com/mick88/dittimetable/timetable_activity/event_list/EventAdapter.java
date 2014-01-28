package com.mick88.dittimetable.timetable_activity.event_list;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
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

	public EventAdapter(Context context, TimetableDay timetableDay, Timetable timetable, AppSettings settings) 
	{
		super(context, R.layout.timetable_event, getTimetableEntries(settings, timetableDay, timetable));
		fontApplicator = new FontApplicator(getContext().getAssets(), TimetableApp.FONT_NAME);
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
	
	private static List<EventItem> getTimetableEntries(AppSettings settings, TimetableDay timetableDay, Timetable timetable)
	{
		List<EventItem> entries = new ArrayList<EventItem>(timetableDay.getEvents().size());
		
		int lastEndHour=0;
		TimetableEvent lastEvent=null;
		
		int currentWeek = Timetable.getCurrentWeek(),
			showWeek = settings.getOnlyCurrentWeek()?currentWeek : 0;
		
		List<SingleEventItem> sameHourEvents = new ArrayList<SingleEventItem>();
		
		for (TimetableEvent event : timetableDay.getEvents(settings)) 
		{
			if (lastEvent != null)
			{
				// add space if there was a time off between the events
				if (lastEndHour < event.getStartHour())
				{
					entries.add(new Space(event.getStartHour() - lastEndHour, lastEndHour));
				}
			}
			
			int numEvents = timetableDay.getNumEventsAt(event.getStartHour(), settings.getHiddenGroups(), showWeek);
			boolean singleEvent = (numEvents == 1);
			
			if (singleEvent)
			{
				entries.add(SingleEventItem.instantiateForEvent(event, timetable));
			}
			else
			{
				if (sameHourEvents.isEmpty()) entries.add(new MultiEventItem(sameHourEvents, timetable));
				else if (sameHourEvents.get(0).getEvent().getStartHour() != event.getStartHour())
				{
					sameHourEvents = new ArrayList<SingleEventItem>();
					entries.add(new MultiEventItem(sameHourEvents, timetable));
				}
					
				sameHourEvents.add(SingleEventItem.instantiateForEvent(event, timetable));
			}
			
			lastEvent = event;
			lastEndHour = event.getEndHour();
		}

		return entries;
	}
}
