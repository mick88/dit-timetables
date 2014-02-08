package com.mick88.dittimetable.timetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.text.TextUtils;
import android.util.Log;


/**
 * Holds information about a single event (lecture)
 * */
public class TimetableEvent implements Comparable<TimetableEvent>, Serializable
{
	private static final long serialVersionUID = 2900289895051796020L;
		
	public static enum ClassType {Other, Lecture, Laboratory, Tutorial};
	public static final int 
		MIN_START_TIME = 8,
		MAX_START_TIME = 22;
	
	private static final String GROUP_SEPARATOR = ", ";
	private static final String logTag = "TimetableEvent";
	
	/*Main event data*/
	protected String 
		name, room, lecturer, weekRange, groupStr="";
	private int 
		id=0, day,
		startHour=0, startMin=0, endMin=0, endHour=0;
	
	protected ClassType type=ClassType.Other;
	protected Set<String> groups = new HashSet<String>();
	protected Set<Integer> weeks;
	
	private boolean 
		custom = true; // event added by user manually

	public void setWeekRange(String weekRange)
	{
		this.weekRange = weekRange;
		decodeWeeks();
	}
	
	public String getDayName()
	{
		return Timetable.DAY_NAMES[day];
	}
	
	public int getDay()
	{
		return day;
	}
	
	public int getId()
	{
		return id;
	}
	
	public ClassType getEventType()
	{
		return getType();
	}

	public String getName()
	{
		return name;
	}
	
	public String getRoom()
	{
		return room;
	}
	
	/**
	 * Gets list of rooms and replaces dividers with NL characters
	 */
	public String getRoomStacked()
	{
		return getRoom()
				.replace(", ", "\n") // alignment fix
				.replace(',', '\n');
	}
	
	public Set<String> getGroups()
	{
		return groups;
	}
	
	/**
	 * get groups that start with the course code specified
	 */
	public Set<String> getGroups(String courseCode)
	{
		Set<String> result = new HashSet<String>();
		for (String group : getGroups())
		{
			if (group.startsWith("DT") == false ||  group.startsWith(courseCode) == true)
				result.add(group);
		}
		
		return result;
	}
	
	public String getWeeks()
	{
		return weekRange;
	}
	
	private String groupToString()
	{
		StringBuilder builder = new StringBuilder();
		int n=0;
		for (String g : groups)
		{
			if (n++ > 0) builder.append(GROUP_SEPARATOR);
			builder.append(g);
		}
		return builder.toString();
	}
	
	/**
	 * Shows group string only for specified course
	 */
	public String getGroupsString(String courseCode)
	{
		StringBuilder builder = new StringBuilder();
		if (groups.isEmpty()) return builder.toString();
		
		int n=0;
		Set<String> groupSet = getGroups(courseCode);
		String[] groups = new String[groupSet.size()];
		groupSet.toArray(groups);
		Arrays.sort(groups);
		
		for (String g : groups)
		{
			if (n++ > 0) builder.append(GROUP_SEPARATOR);
			builder.append(g);
		}
		return builder.toString();
	}
	
	public int getStartHour()
	{
		return startHour;
	}
	
	public int getEndHour()
	{
		return endHour;
	}
	
	public String getLecturer()
	{
		return lecturer;
	}
	
	public CharSequence getStartTime()
	{
		return String.format(Locale.getDefault(),  "%d:%02d", getStartHour(), getStartMin());
	}
	
	/**
	 * Gets duration of this event in hours
	 */
	public int getDuration()
	{
		return getEndHour() - getStartHour();
	}
	
	public boolean isToday()
	{
		return Timetable.getTodayId(false) == this.day;
	}
	
	public boolean isEventOn()
	{
		return isEventOn(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
	}
	
	public boolean isEventOn(int hour)
	{
		return (hour >= getStartHour() && hour < getEndHour() && isToday());
	}
	
	public CharSequence getEndTime()
	{
		return String.format(Locale.getDefault(),  "%d:%02d", getEndHour(), getEndMin());
	}
	
	public void setRoom(String room)
	{
		this.room = room;
	}
	
	public boolean isInWeek(int week)
	{
		return week == 0 || weeks.isEmpty() || weeks.contains(week);
	}
	
	private TimetableEvent()
	{
		weeks = new HashSet<Integer>();
	}
	
	public TimetableEvent(int day)
	{
		this();
		this.day = day;		
	}
	
	/**
	 * Creates new object by importing data from string
	 */
	@Deprecated
	public TimetableEvent(String importString, int day)
	{
		this(day);
		importFromString(importString);
	}
	
	/**
	 * Returns true if class should be shown to user.
	 * It will be shown if at least 1 group in this class is not hidden
	 * @param hiddenGroups collection of groups that are hidden
	 * @return
	 */
	public boolean isVisibleForGroupExcluding(Set<String> hiddenGroups)
	{
		if (groups.isEmpty()) return true;
		
		for (String groupCode : groups)
		{
			if (hiddenGroups.contains(groupCode) == false)
				return true;
		}
		
		return false;
	}
	
	void addGroup(String group)
	{
		if (TextUtils.isEmpty(group) == false)
		{
			groups.add(group);
			groupStr = groupToString();
		}
	}
	
	public String getGroupStr()
	{
		return groupStr;
	}
	
	public void setGroups(String groupString)
	{
		String [] grps = groupString.split(",");
		for (String s : grps)
		{
			int end = s.indexOf('-');
			
			String group = ((end==-1)?s:s.substring(0, end)).trim();

			addGroup(group);
		}
	}
	
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "%s: %s (%s)", getStartTime(), getName(), room);
	}
	
	final String exportItemSeparator = ";";
	public CharSequence export()
	{		
		StringBuilder builder = 
				new StringBuilder();
		builder.append(getId()).append(exportItemSeparator);		
		builder.append(getName()).append(exportItemSeparator);
		builder.append(room).append(exportItemSeparator);
		builder.append(getLecturer()).append(exportItemSeparator);
		
		builder.append(getStartHour()).append(exportItemSeparator);
		builder.append(getStartMin()).append(exportItemSeparator);
		
		builder.append(getEndHour()).append(exportItemSeparator);
		builder.append(getEndMin()).append(exportItemSeparator);
		
		builder.append(weekRange).append(exportItemSeparator);
		builder.append(getType().name()).append(exportItemSeparator);
		builder.append(groupToString().replace(exportItemSeparator, "")).append(exportItemSeparator);		
		
		return builder.toString();
	}
	
	private void decodeWeeks()
	{
		weeks.clear();
		String [] sections = weekRange.split(",");
		for (String section : sections)
		{
			String sectionTrimmed = section.trim();
			if (sectionTrimmed.indexOf('-') > -1) // is range
			{
				String [] rangeValues = sectionTrimmed.split("-");
				if (rangeValues.length == 2)
				{
					int min = Integer.parseInt(rangeValues[0]),
							max = Integer.parseInt(rangeValues[1]);
					while (min <= max)
					{
						weeks.add(min++);
					}
				}
				else
				{
					Log.e(logTag, "Cannot decode week range "+sectionTrimmed);
				}
			}
			else // is single week  value
			{
				try
				{
					Integer week = Integer.parseInt(sectionTrimmed);
					weeks.add(week);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(logTag, "Cannot parse week "+sectionTrimmed);
				}
			}
		}
	}
	
	/**
	 * imports saved event from file
	 */
	@Deprecated
	public void importFromString(String string)
	{
		if (TextUtils.isEmpty(string)) 
		{
			return;
		}
		try
		{
			String [] fields = string.split(exportItemSeparator, -1);
			int field = 0;
			setId(Integer.valueOf(fields[field++]));
			setName(fields[field++]);
			room = fields[field++];
			setLecturer(fields[field++]);
			
			setStartHour(Integer.valueOf(fields[field++]));
			setStartMin(Integer.valueOf(fields[field++]));
			setEndHour(Integer.valueOf(fields[field++]));
			setEndMin(Integer.valueOf(fields[field++]));
			
			weekRange = fields[field++];
			setType(ClassType.valueOf(fields[field++]));
			
			if (fields.length >= field+1)
			{
				String [] g = fields[field++].split(GROUP_SEPARATOR);
				for (String s : g)
				{
					addGroup(s);
				}
			}
			decodeWeeks();
		}
		catch (Exception e)
		{
			e.printStackTrace();		
			Log.w(logTag, "Cannot parse event "+string);
		}
	}
	
	public boolean isValid()
	{
		return getStartHour() >= MIN_START_TIME && getEndHour() > getStartHour();
	}
	
	/**
	 * Gets event human-readable string like 9:00 - 11:00
	 * @return
	 */
	public CharSequence getEventTimeString()
	{
		return new StringBuilder(getStartTime())
			.append(" - ")
			.append(getEndTime());
	}

	@Override
	public int compareTo(TimetableEvent another)
	{
		if (another.getStartHour() > this.getStartHour()) return -1;
		else if (another.getStartHour() < this.getStartHour()) return 1;
		
		return groupStr.compareTo(another.groupStr);
	}

	@Override
	public int hashCode()
	{
		return getId();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof TimetableEvent)
		{
			return ((TimetableEvent) o).getId() == this.getId();
		}
		else return false;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setStartHour(int startHour)
	{
		this.startHour = startHour;
	}

	public int getStartMin()
	{
		return startMin;
	}

	public void setStartMin(int startMin)
	{
		this.startMin = startMin;
	}

	public void setEndHour(int endHour)
	{
		this.endHour = endHour;
	}

	public int getEndMin()
	{
		return endMin;
	}

	public void setEndMin(int endMin)
	{
		this.endMin = endMin;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ClassType getType()
	{
		return type;
	}

	public void setType(ClassType type)
	{
		this.type = type;
	}

	public void setLecturer(String lecturer)
	{
		this.lecturer = lecturer;
	}
	
	public void setCustom(boolean custom)
	{
		this.custom = custom;
	}
	
	public boolean isCustom()
	{
		return custom;
	}
}
