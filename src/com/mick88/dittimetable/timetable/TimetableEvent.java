package com.mick88.dittimetable.timetable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.event_details.EventDetailsSwipableActivity;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.utils.FontApplicator;


/**
 * Holds information about a single event (lecture)
 * */
public class TimetableEvent implements Comparable<TimetableEvent>, EventItem, Serializable
{
	private static final long serialVersionUID = 2900289895051796020L;

	public static class EventViewHolder
	{
		protected final TextView tvEventTime, tvEventLocation, tvEventTitle, tvEventType, tvEventLecturer, tvEventGroup;
		public final View background, eventTile;
		
		public EventViewHolder(TextView tvEventTime, TextView tvEventLocation,
				TextView tvEventTitle, TextView tvEventType,
				TextView tvEventLecturer, TextView tvEventGroup, View background, View eventTile) {
			this.tvEventTime = tvEventTime;
			this.tvEventLocation = tvEventLocation;
			this.tvEventTitle = tvEventTitle;
			this.tvEventType = tvEventType;
			this.tvEventLecturer = tvEventLecturer;
			this.tvEventGroup = tvEventGroup;
			this.background = background;
			this.eventTile = eventTile;
		}
		
		public EventViewHolder(View view)
		{
			this((TextView)view.findViewById(R.id.eventTime),
					(TextView)view.findViewById(R.id.eventLocation),
					(TextView)view.findViewById(R.id.eventTitle),
					(TextView)view.findViewById(R.id.eventType),
					(TextView)view.findViewById(R.id.eventLecturer),
					(TextView)view.findViewById(R.id.eventGroup),
					view.findViewById(R.id.timetable_event_small),
					view.findViewById(R.id.timetable_event_small));
		}
	}
		
	public static enum ClassType {Other, Lecture, Laboratory, Tutorial};
	public static final int 
		MIN_START_TIME = 8,
		MAX_START_TIME = 22;
	
	private static final String GROUP_SEPARATOR = ", ";
	final static String logTag = "TimetableEvent";
	
	/*Main event data*/
	private String name="";

	protected String room="";

	private String lecturer = "";

	protected String weekRange="";

	protected String groupStr="";
	private int id=0;

	private int startHour=0;

	private int startMin=0;

	private int endMin=0;

	private int endHour=0;
	private ClassType type=ClassType.Other;
	Set<String> groups = new HashSet<String>();
	
	private final int day;
	
	/**
	 * Stores parsed list of weeks when event is on
	 */
	final Set<Integer> weeks;
	
	private boolean complete =false;
	
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
	
	public TimetableEvent(int day)
	{
		this.day = day;
		weeks = new HashSet<Integer>();
	}
	
	/**
	 * Creates new object by importing data from string
	 */
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
			setComplete(true);
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
	public int getViewType()
	{
		return ITEM_TYPE_EVENT;
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

	@Override
	public View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent, FontApplicator fontApplicator, boolean allowHighlight, final Timetable timetable)
	{
		View view = convertView;
		EventViewHolder viewHolder;
		if (view == null)
		{
			view = layoutInflater.inflate(R.layout.timetable_event, parent, false);
			if (fontApplicator != null) fontApplicator.applyFont(view);
			viewHolder = new EventViewHolder(view);
			view.setTag(viewHolder);
		}
		else viewHolder = (EventViewHolder) view.getTag();
		
		if (allowHighlight && isEventOn())
			viewHolder.background.setBackgroundResource(R.drawable.event_selected_selector);
		else
			viewHolder.background.setBackgroundResource(R.drawable.event_selector);
		
		viewHolder.eventTile.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Context context = v.getContext().getApplicationContext();
				Intent intent = new Intent(context, EventDetailsSwipableActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(EventDetailsSwipableActivity.EXTRA_SELECTED_EVENT, TimetableEvent.this);
				intent.putExtra(EventDetailsSwipableActivity.EXTRA_DAY, timetable.getDay(day));
				context.startActivity(intent);				
			}
		});
		
		viewHolder.tvEventGroup.setText(getGroupStr());
		viewHolder.tvEventLecturer.setText(getLecturer());
		viewHolder.tvEventLocation.setText(getRoom());
		viewHolder.tvEventTime.setText(getEventTimeString());
		viewHolder.tvEventType.setText(getEventType().toString());
		viewHolder.tvEventTitle.setText(getName());
		
		int colourRes = 0;
		switch (getEventType())
		{
			case Laboratory:
				colourRes = R.color.color_laboratory;
				break;
			case Lecture:
				colourRes = R.color.color_lecture;
				break;
			case Tutorial:
				colourRes = R.color.color_tutorial;
				break;			
		}
		
		if (colourRes != 0)
		{
			viewHolder
			.tvEventType
			.setTextColor(viewHolder
					.tvEventType
					.getContext()
					.getResources()
					.getColor(colourRes));
		}
		return view;		
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

	public boolean isComplete()
	{
		return complete;
	}

	public void setComplete(boolean complete)
	{
		this.complete = complete;
	}

	public void setLecturer(String lecturer)
	{
		this.lecturer = lecturer;
	}
}
