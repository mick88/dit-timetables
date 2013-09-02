package com.mick88.dittimetable.timetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import android.content.Context;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.list.MultiEvent;
import com.mick88.dittimetable.list.Space;

/**
 * ontains list of classes in a day
 * 
 */
public class TimetableDay implements Serializable
{
	private static final long serialVersionUID = 1L;
	Timetable timetable=null;
	final String name;
	int id=-1;
	final String logTag = "TimetableDay";
	private List<TimetableEvent> events = new ArrayList<TimetableEvent>();
	
	public TimetableDay(String name, Timetable timetable)
	{
		this.name = name;
		this.timetable = timetable;
	}
	
	public void clearEvents()
	{
		events.clear();
	}
	
	public void sortEvents()
	{
		synchronized (events)
		{
			Collections.sort(events);
		}		
	}
	
	public void addClass(TimetableEvent c)
	{
		synchronized (events)
		{
			events.add(c);
		}		
	}
	
	public String getName()
	{
		return name;
	}
	
	public CharSequence getShortName()
	{
		return name.subSequence(0, 3);
	}
	
	public int getNumEvents(int hour, Set<String> hiddenGroups, int week)
	{
		int n=0;
		for (TimetableEvent event : events) if (event.getStartHour() == hour && event.isInWeek(week))
		{
			if (event.isGroup(hiddenGroups)) n++;
		}
		return n;
	}
	
	public List<TimetableEvent> getClasses()
	{
		return events;
	}
	
	/**
	 * Get Set of lectures at this time
	 * @return
	 */
	public Set<TimetableEvent> getCurrentEvents()
	{
		Set<TimetableEvent> result = new HashSet<TimetableEvent>();
		if (isToday())
		{
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			for (TimetableEvent event : events)
				if (event.isEventOn(hour)) result.add(event);
		}
		return result;
	}
	
	public int parseHtmlEvent(Element element, Context context, boolean allowCache)
	{
		int n=0;
		TimetableEvent c = new TimetableEvent(element, timetable, context, allowCache, this.name);
		if (c.isValid() /*&& c.isGroup(timetable.getHiddenGroups())*/) 
		{
			addClass(c);
			n++;
		}

		return n;
	}
	
	
	@Override
	public String toString()
	{
		if (events.isEmpty()) return new String();
		
		StringBuilder builder = new StringBuilder(name);
		
		for (TimetableEvent c : events)
		{
			builder.append('\n');
			builder.append(c.toString());
			
		}
		return builder.toString();
	}
	
	public String toString(Set<String> hiddenGroups, int week)
	{
		if (events.isEmpty()) return new String();
		int n=0;
		StringBuilder builder = new StringBuilder(name);
		
		for (TimetableEvent event : events) if (event.isInWeek(week) && event.isGroup(hiddenGroups))
		{
			builder.append('\n');
			builder.append(event.toString());
			n++;
			
		}
		if (n == 0) return new String();
		else return builder.toString();
	}
	
	private final String EXPORT_DAY_SEPARATOR = "\n";
	public CharSequence export()
	{
		StringBuilder builder = new StringBuilder();
		for (TimetableEvent event : events)
		{
			builder.append(event.export()).append(EXPORT_DAY_SEPARATOR);
		}
		return builder;
	}
	
	public int importFromString(String string)
	{
		int n=0;
		String [] events = string.split(EXPORT_DAY_SEPARATOR);
		for (String eventString : events)
		{
			TimetableEvent event = new TimetableEvent(eventString, timetable, this.name);
			if (event.isValid() /*&& event.isGroup(timetable.hiddenGroups)*/)
			{
				n++;
				addClass(event);
			}
		}
		return n;
	}
	
	public boolean isToday()
	{
		return timetable.getToday(false) == this;
	}
	
	public List<EventItem> getTimetableEntries()
	{
		List<EventItem> entries = new ArrayList<EventItem>(events.size());
		
		int lastEndHour=0;
		TimetableEvent lastEvent=null;
		
		AppSettings settings = timetable.getSettings();
		int currentWeek = Timetable.getCurrentWeek(),
			showWeek = settings.getOnlyCurrentWeek()?currentWeek : 0;
		
		List<TimetableEvent> sameHourEvents = new ArrayList<TimetableEvent>();
		
		synchronized (events)
		{
			for (TimetableEvent event : events) 
				if (event.isGroup(settings.getHiddenGroups()) && event.isInWeek(showWeek))
			{
				if (lastEvent != null)
				{
					// add space if there was a time off between the events
					if (lastEndHour < event.getStartHour())
					{
						entries.add(new Space(event.getStartHour() - lastEndHour, lastEndHour));
					}
				}
				
				int numEvents = getNumEvents(event.getStartHour(), settings.getHiddenGroups(), showWeek);
				boolean singleEvent = (numEvents == 1);
				
				if (singleEvent)
				{
					entries.add(event);
				}
				else
				{
					if (sameHourEvents.isEmpty()) entries.add(new MultiEvent(sameHourEvents));
					else if (sameHourEvents.get(0).getStartHour() != event.getStartHour())
					{
						sameHourEvents = new ArrayList<TimetableEvent>();
						entries.add(new MultiEvent(sameHourEvents));
					}
						
					sameHourEvents.add(event);
				}
				
				lastEvent = event;
				lastEndHour = event.getEndHour();
			}

		}

		return entries;
	}
	
	public void downloadAdditionalInfo(Context context)
	{
		for (TimetableEvent event : events) if (event.isComplete() == false)
		{
			if (timetable.isDisposed()) break;
			event.downloadAdditionalInfo(context);
		}
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof TimetableDay)
		{
			return ((TimetableDay) o).id == id;
		}
		else return super.equals(o);
	}
}
