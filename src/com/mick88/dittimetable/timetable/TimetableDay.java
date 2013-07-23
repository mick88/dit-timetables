package com.mick88.dittimetable.timetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Element;

import android.content.Context;

/*Contains list of classes in a day*/
public class TimetableDay
{
	Timetable timetable=null;
	String name = "";
	final String logTag = "TimetableDay";
	private ArrayList<TimetableEvent> classes = new ArrayList<TimetableEvent>();
	
	public TimetableDay(String name, Timetable timetable)
	{
		this.name = name;
		this.timetable = timetable;
	}
	
	public void clearEvents()
	{
		classes.clear();
/*		for (int i=0; i < numClassesAt.length; i++)
		{
			numClassesAt[i]=0;
		}*/
	}
	
	public void sortEvents()
	{
		Collections.sort(classes);
	}
	
	public void addClass(TimetableEvent c)
	{
//		numClassesAt[c.getStartHour()] += 1;
		classes.add(c);
	}
	
	@Deprecated
	public int getNumClassesAt(int hour, int week)
	{
//		return numClassesAt[hour];
		return getNumClasses(hour, timetable.getHiddenGroups(), week);
	}
	
	public int getNumClasses(int hour, List<String> hiddenGroups, int week)
	{
		int n=0;
		for (TimetableEvent event : classes) if (event.getStartHour() == hour && event.isInWeek(week))
		{
			if (event.isGroup(hiddenGroups)) n++;
		}
		return n;
	}
	
	public ArrayList<TimetableEvent> getClasses()
	{
		return classes;
	}
	
	public int parseHtmlEvent(Element element, Context context, boolean allowCache)
	{
		int n=0;
		TimetableEvent c = new TimetableEvent(element, timetable, context, allowCache);
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
		if (classes.isEmpty()) return new String();
		
		StringBuilder builder = new StringBuilder(name);
		
		for (TimetableEvent c : classes)
		{
			builder.append('\n');
			builder.append(c.toString());
			
		}
		return builder.toString();
	}
	
	public String toString(List<String> hiddenGroups, int week)
	{
		if (classes.isEmpty()) return new String();
		int n=0;
		StringBuilder builder = new StringBuilder(name);
		
		for (TimetableEvent event : classes) if (event.isInWeek(week) && event.isGroup(hiddenGroups))
		{
			builder.append('\n');
			builder.append(event.toString());
			n++;
			
		}
		if (n == 0) return new String();
		else return builder.toString();
	}
	
	private final String exportItemSeparator = "\n";
	public CharSequence export()
	{
		StringBuilder builder = new StringBuilder();
		for (TimetableEvent event : classes)
		{
			builder.append(event.export()).append(exportItemSeparator);
		}
		return builder;
	}
	
	public int importFromString(String string)
	{
		int n=0;
		String [] events = string.split(exportItemSeparator);
		for (String eventString : events)
		{
			TimetableEvent event = new TimetableEvent(eventString, timetable);
			if (event.isValid() /*&& event.isGroup(timetable.hiddenGroups)*/)
			{
				n++;
				addClass(event);
			}
		}
		return n;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}	
}
