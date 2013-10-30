package com.mick88.dittimetable.timetable;

import java.io.FileOutputStream;
import java.io.IOException;
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
import com.mick88.dittimetable.list.EventAdapter.EventItem;
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
	protected String name="", room="", lecturer = "", weekRange="", groupStr="";
	protected int id=0,
			 startHour=0,
				startMin=0,
				endMin=0,
			endHour=0;
	ClassType type=ClassType.Other;
	Set<String> groups = new HashSet<String>();
	
	private final int day;
	
	/**
	 * Stores parsed list of weeks when event is on
	 */
	final Set<Integer> weeks;
	
	boolean complete =false;
	// changed to true when event info is loaded from website
	protected transient boolean updated = false;
	
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
	
	public ClassType getType()
	{
		return type;
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
	
	public ClassType getClassType()
	{
		return type;
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
	
	public String getRoomShort()
	{
		return room;
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
		return String.format(Locale.getDefault(),  "%d:%02d", startHour, startMin);
	}
	
	/**
	 * Gets duration of this event in hours
	 */
	public int getLength()
	{
		return endHour - startHour;
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
		return (hour >= startHour && hour < endHour && isToday());
	}
	
	public CharSequence getEndTime()
	{
		return String.format(Locale.getDefault(),  "%d:%02d", endHour, endMin);
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
	
	public boolean isGroup(Set<String> hiddenGroups)
	{
		if (groups.isEmpty()) return true;
		/*Returns true if class should be shown to user.
		 * It will be shown if at least 1 group in this class is not hidden */
		
		for (String groupCode : groups)
		{
			if (hiddenGroups.contains(groupCode) == false)
			{
				return true;
			}
		}
		
		return false;
	}
	
	void addGroup(String group)
	{
		if (TextUtils.isEmpty(group) == false && groups.contains(group) == false)
		{
			groups.add(group);
			groupStr = groupToString();
		}
	}
	
	public String getGroupStr()
	{
		return groupStr;
	}

	public boolean isComplete()
	{
		return complete;
	}
	
	/**
	 * Tells whether the event details have just been downloaded
	 * @return
	 */
	public boolean isUpdated()
	{
		return updated;
	}
	
	void setGroups(String groupString)
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
		return String.format(Locale.ENGLISH, "%d:%02d-%d:%02d %s (%s)", startHour, startMin, endHour, endMin, name, room);
	}
	
	final String exportItemSeparator = ";";
	public CharSequence export()
	{		
		StringBuilder builder = 
				new StringBuilder();
		builder.append(id).append(exportItemSeparator);		
		builder.append(name).append(exportItemSeparator);
		builder.append(room).append(exportItemSeparator);
		builder.append(lecturer).append(exportItemSeparator);
		
		builder.append(startHour).append(exportItemSeparator);
		builder.append(startMin).append(exportItemSeparator);
		
		builder.append(endHour).append(exportItemSeparator);
		builder.append(endMin).append(exportItemSeparator);
		
		builder.append(weekRange).append(exportItemSeparator);
		builder.append(type.name()).append(exportItemSeparator);
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
			id = Integer.valueOf(fields[field++]);
			name = fields[field++];
			room = fields[field++];
			lecturer = fields[field++];
			
			startHour = Integer.valueOf(fields[field++]);
			startMin = Integer.valueOf(fields[field++]);
			endHour = Integer.valueOf(fields[field++]);
			endMin = Integer.valueOf(fields[field++]);
			
			weekRange = fields[field++];
			type = ClassType.valueOf(fields[field++]);
			
			if (fields.length >= field+1)
			{
				String [] g = fields[field++].split(GROUP_SEPARATOR);
				for (String s : g)
				{
					addGroup(s);
				}
			}
			complete=true;
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
		return startHour >= MIN_START_TIME && endHour > startHour;
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
		if (another.startHour > this.startHour) return -1;
		else if (another.startHour < this.startHour) return 1;
		
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
		return id;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof TimetableEvent)
		{
			return ((TimetableEvent) o).id == this.id;
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
				intent.putExtra(EventDetailsSwipableActivity.EXTRA_SETTINGS, timetable.getSettings());
				intent.putExtra(EventDetailsSwipableActivity.EXTRA_DAY, timetable.getDay(day));
				context.startActivity(intent);				
			}
		});
		
		viewHolder.tvEventGroup.setText(getGroupStr());
		viewHolder.tvEventLecturer.setText(getLecturer());
		viewHolder.tvEventLocation.setText(getRoom());
		viewHolder.tvEventTime.setText(getEventTimeString());
		viewHolder.tvEventType.setText(getClassType().toString());
		viewHolder.tvEventTitle.setText(getName());
		
		int colourRes = 0;
		switch (getType())
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
}
