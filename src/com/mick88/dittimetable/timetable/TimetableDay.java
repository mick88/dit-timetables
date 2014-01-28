package com.mick88.dittimetable.timetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mick88.dittimetable.settings.AppSettings;

/**
 * ontains list of classes in a day
 * 
 */
public class TimetableDay implements Serializable
{
	private static final long serialVersionUID = 1L;
	@Deprecated
	private final String EXPORT_DAY_SEPARATOR = "\n";
	private int id;
	protected List<TimetableEvent> events = new ArrayList<TimetableEvent>();	
	
	private TimetableDay()
	{
		
	}
	
	public TimetableDay(int id)
	{
		this();
		this.id = id;
	}
	
	public void clearEvents()
	{
		synchronized (events)
		{
			events.clear();
		}
	}
	
	public void sortEvents()
	{
		synchronized (events)
		{
			Collections.sort(events);
		}		
	}
	
	public void addEvent(TimetableEvent event)
	{
		synchronized (events)
		{
			events.add(event);
		}		
	}
	
	public String getName()
	{
		return Timetable.DAY_NAMES[getId()];
	}
	
	public CharSequence getShortName()
	{
		return getName().subSequence(0, 3);
	}
	
	public int getNumEventsAt(int hour, Set<String> hiddenGroups, int week)
	{
		int n=0;
		for (TimetableEvent event : events) 
			if (event.getStartHour() == hour && event.isInWeek(week) && event.isVisibleForGroupExcluding(hiddenGroups))
				n++;
		return n;
	}
	
	public List<TimetableEvent> getEvents()
	{
		return events;
	}
	
	public int getEventCount(AppSettings settings)
	{
		int result = 0;
		int currentWeek = Timetable.getCurrentWeek(),
			showWeek = settings.getOnlyCurrentWeek()?currentWeek : 0;
		
		for (TimetableEvent event : events) 
			if (event.isVisibleForGroupExcluding(settings.getHiddenGroups()) && event.isInWeek(showWeek))
				result++;
		return result;
	}
	
	public void getGroups(Set<String> groupSet)
	{
		for (TimetableEvent event : events)
			groupSet.addAll(event.getGroups());
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
	
	/**
	 * Gets string representing hours from first to last event on this day
	 * @param appSettings
	 * @return
	 */
	public CharSequence getHoursRange(AppSettings appSettings)
	{
		List<TimetableEvent> events = getEvents(appSettings);
		if (events.isEmpty()) return null;
		CharSequence start = events.get(0).getStartTime(),
				end = events.get(events.size()-1).getEndTime();
		
		return new StringBuilder(start).append(" - ").append(end);
	}
	
	
	@Override
	public String toString()
	{
		if (events.isEmpty()) return new String();
		
		StringBuilder builder = new StringBuilder(getName());
		
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
		StringBuilder builder = new StringBuilder(getName());
		
		for (TimetableEvent event : events) if (event.isInWeek(week) && event.isVisibleForGroupExcluding(hiddenGroups))
		{
			builder.append('\n');
			builder.append(event.toString());
			n++;
			
		}
		if (n == 0) return new String();
		else return builder.toString();
	}
	
	@Deprecated
	public CharSequence export()
	{
		StringBuilder builder = new StringBuilder();
		for (TimetableEvent event : events)
		{
			builder.append(event.export()).append(EXPORT_DAY_SEPARATOR);
		}
		return builder;
	}
	
	@Deprecated
	public int importFromString(String string)
	{
		int n=0;
		String [] events = string.split(EXPORT_DAY_SEPARATOR);
		for (String eventString : events)
		{
			TimetableEvent event = new TimetableEvent(eventString, getId());
			if (event.isValid() /*&& event.isGroup(timetable.hiddenGroups)*/)
			{
				n++;
				addEvent(event);
			}
		}
		return n;
	}
	
	public boolean isToday()
	{
		return Timetable.getTodayId(false) == this.getId();
	}
	
	public List<TimetableEvent> getEvents(AppSettings settings)
	{
		List<TimetableEvent> events = new ArrayList<TimetableEvent>();
		
		int	showWeek = settings.getOnlyCurrentWeek() ? Timetable.getCurrentWeek() : 0;
		
		for (TimetableEvent event : this.events) 
			if (event.isVisibleForGroupExcluding(settings.getHiddenGroups()) && event.isInWeek(showWeek))
			{
				events.add(event);
			}
				
		return events;
	}
	
	
	
	@Override
	public int hashCode()
	{
		return getId();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof TimetableDay)
		{
			return ((TimetableDay) o).getId() == getId();
		}
		else return super.equals(o);
	}

	public int getId()
	{
		return id;
	}
	
	public boolean isEmpty(AppSettings settings)
	{
		return getEventCount(settings) == 0;
	}

	public boolean isEmpty()
	{
		return events.isEmpty();
	}
}
