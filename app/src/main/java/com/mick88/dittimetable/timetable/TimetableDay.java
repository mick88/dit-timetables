package com.mick88.dittimetable.timetable;

import com.mick88.dittimetable.settings.AppSettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * ontains list of classes in a day
 * 
 */
public class TimetableDay implements Serializable
{
	private static final long serialVersionUID = 1L;
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
	
	public int getNumEventsAt(int hour, AppSettings appSettings, int week)
	{
		int n=0;
		for (TimetableEvent event : getEvents(appSettings)) 
			if (event.getStartHour() == hour && event.isInWeek(week))
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
			if (event.isVisibleForGroupExcluding(settings.getHiddenGroups()) && event.isInWeek(showWeek) && settings.getHiddenModules().contains(event.getName()) == false)
				result++;
		return result;
	}
	
	public void getGroups(Set<String> groupSet)
	{
		for (TimetableEvent event : events)
			groupSet.addAll(event.getGroups());
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
		if (events.isEmpty()) return "";
		
		StringBuilder builder = new StringBuilder(getName());
		
		for (TimetableEvent c : events)
		{
			builder.append('\n');
			builder.append(c.toString());
			
		}
		return builder.toString();
	}
	
	public String toString(AppSettings appSettings)
	{
		if (events.isEmpty()) return "";
		int n=0;
		StringBuilder builder = new StringBuilder(getName());
		
		for (TimetableEvent event : getEvents(appSettings))
		{
			builder.append('\n');
			builder.append(event.toString());
			n++;
			
		}
		if (n == 0) return "";
		else return builder.toString();
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
			if (event.isVisibleForGroupExcluding(settings.getHiddenGroups()) && event.isInWeek(showWeek) && settings.getHiddenModules().contains(event.getName()) == false)
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
