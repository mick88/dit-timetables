package com.mick88.dittimetable.timetable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.mick88.dittimetable.event_details.EventDetailsActivity;
import com.mick88.dittimetable.event_details.EventDetailsSwipableActivity;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.utils.FontApplicator;
import com.mick88.dittimetable.web.Connection;


/**
 * Holds information about a single event (lecture)
 * */
public class TimetableEvent implements Comparable<TimetableEvent>, EventItem, Serializable
{
	private static final long serialVersionUID = 2900289895051796020L;

	private static class EventViewHolder
	{
		protected final TextView tvEventTime, tvEventLocation, tvEventTitle, tvEventType, tvEventLecturer, tvEventGroup;
		protected final View background, eventTile;
		
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
	
	private static final String CHAR_NBSP = "\u00A0";	
	public static enum ClassType {Other, Lecture, Laboratory, Tutorial};
	
	private static final String GROUP_SEPARATOR = ", ";
	final static String logTag = "TimetableEvent";
	final static String 
			COLOR_NAME = "#987E06", 
			COLOR_GROUP = "#987E06",
			COLOR_TYPE = "#E32198", //E32198
			COLOR_LOCATION = "#062F98", //062F98
			COLOR_LECTURER = "#069810", //069810
			COLOR_TIME = "#940AA8", //940AA8
			COLOR_WEEKS = "#177F23";
			;
	
	/*Main event data*/
	private String name="", room="", lecturer = "", weekRange="", groupStr="";
	private int id=0,
			 startHour=0,
				startMin=0,
				endMin=0,
			endHour=0;
	private ClassType type=ClassType.Other;
	Set<String> groups = new HashSet<String>();
	
	private final int day;
	
	/**
	 * Stores parsed list of weeks when event is on
	 */
	final Set<Integer> weeks;
	
	boolean valid=true, complete =false;
	
	String StripNbsp(String string)
	{
		return string.replaceAll(CHAR_NBSP, "");
	}
	
	private String getFileName()
	{
		return String.format(Locale.getDefault(), "%d.html", id);
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
	
	static final int 
		ID_DAY = 2,
		ID_START = 3,
		ID_FINISH = 4,
		ID_ROOM = 5,
		ID_NAME = 8,
		ID_TYPE = 9;	
	
	private TimetableEvent(Timetable timetable, int day)
	{
		this.day = day;
		weeks = new HashSet<Integer>();
	}
	
	/**
	 * Creates new object by parsing data and loading/downloading additional data
	 */
	public TimetableEvent(Element table, Timetable timetable, Context context, boolean allowCache, int day)
	{
		this(timetable, day);
		parseNewHtmlTable(table, context, allowCache, timetable);
	}
	
	/**
	 * Creates new object by importing data from string
	 */
	public TimetableEvent(String importString, Timetable timetable, int day)
	{
		this(timetable, day);
		importFromString(importString, timetable);
//		this.duration = this.endTime - this.startTime;
	}
	
	/**
	 * Parse start/end time of the event
	 */
	private void parseTime(String s)
	{
		String[] hours = StripNbsp(s).split("-");
		try
		{
			startHour = Integer.parseInt(hours[0].split(":")[0]);
			endHour = Integer.parseInt(hours[1].split(":")[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
	
	void addGroup(String group, Timetable timetable)
	{
		if (TextUtils.isEmpty(group) == false && groups.contains(group) == false)
		{
			groups.add(group);
			timetable.addClassGroup(group);
			groupStr = groupToString();
		}
	}
	
	public String getGroupStr()
	{
		return groupStr;
	}
	
	private void parseType(String s)
	{
		if (s.contains("Lecture")) type = ClassType.Lecture;
		else if (s.contains("Laboratory")) type = ClassType.Laboratory;
		else if (s.contains("Tutorial")) type = ClassType.Tutorial;
		else type = ClassType.Other;
		
	}
	
	/**
	 * Loads additional information about event from web timetables.
	 */
	public void downloadAdditionalInfo(Context context, Timetable timetable)
	{
		Connection connection = timetable.getConnection();
		String uri = String.format(Locale.getDefault(), "?reqtype=eventdetails&eventId=%s%%7C%d", Timetable.getDataset(), id);

		String content = connection.getContent(uri);
		if (parseAdditionalInfo(content, timetable))
		{
			complete = true;
			try
			{
				saveAdditionalInfo(context, content);
			} catch (IOException e)
			{
			
				e.printStackTrace();
			}
		}
	}
	
	public boolean isComplete()
	{
		return complete;
	}

	private boolean loadAdditionalInfo(Context context, Timetable timetable)
	{
		String filename = getFileName();	
		StringBuffer sb = new StringBuffer();
		try
		{
			if (context.getFileStreamPath(filename).exists() == false)
			{
				Log.i(logTag, "Event  could not be loaded from file. File does not exist "+filename);
				return false;
			}
			
			final int BUFFER_SIZE = 30000;
			FileInputStream f = context.openFileInput(filename);
			
			byte[] buffer = new byte[BUFFER_SIZE];			
			
			while (f.read(buffer) > 0)
			{
				String line = new String(buffer);
				sb.append(line);
				
				buffer = new byte[BUFFER_SIZE];
			}
			
			f.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		if (parseAdditionalInfo(sb.toString(), timetable))
		{
			return true;
		}
		return false;
	}
	
	private void saveAdditionalInfo(Context context, String content) throws IOException
	{
		String filename = getFileName();
		FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);			
		byte[] buffer = content.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}
	
	/**
	 * parses the additional info page
	 */
	private boolean parseAdditionalInfo(String content, Timetable timetable)
	{
		if (content == null) return false;
		Document doc = Jsoup.parse(content);
		Element table = doc.select("table.eventdetails").first();
		if (table == null) return false;
		
		HashMap<String, String> tableHeaders = new HashMap<String, String>();
		HashMap<String, String> tableValues = new HashMap<String, String>();
		
		Elements headers = table.select("th");		
		for (Element header : headers)
		{
			tableHeaders.put(header.id(), header.text());
		}
		headers.clear();
		
		Elements values = table.select("td");
		for (Element value : values)
		{
			String val = value.text();
			if (TextUtils.isEmpty(val)) continue;
			String h = tableHeaders.get(value.attr("headers"));
			tableValues.put(h, val);
		}
		values.clear();
		tableHeaders.clear();
		if (tableValues.size() == 0) return false;
		
		try
		{
//			name = tableValues.get("Module").trim().replaceFirst("CMPU\\s[\\d]{4}\\s-\\s", "");
			name = tableValues.get("Module").trim().replaceFirst("[A-Z]{4}\\s[\\d]{4}\\s-\\s", "");
			String [] grps = tableValues.get("Class Subgroup").split(",");
			for (String s : grps)
			{
				int end = s.indexOf('-');
				
				String group = ((end==-1)?s:s.substring(0, end)).trim();
	
				addGroup(group, timetable);
			}
			weekRange = tableValues.get("Week numbers");
			decodeWeeks();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		tableValues.clear();	
		return true;
	}
	
	/** 
	 * Parses the new timetable
	 * @param	table	Element containing event table 
	 */
	public void parseNewHtmlTable(Element table, Context context, boolean allowCache, Timetable timetable)
	{
		valid=true;
		try
		{
			String idString = table.id();
			if (idString.charAt(0) == 'c')
			{
				id = Integer.valueOf(idString.substring(1));
			}
			else Log.e(logTag, "Event id incorrect: "+idString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
			
		Elements elements = table.select("td");

		for (Element element : elements)
		{
			EventElement e = new EventElement(element);
			if (e.color.equalsIgnoreCase(COLOR_NAME))
			{
				if (TextUtils.isEmpty(name)) name = e.getText();
			}
			else if (e.color.equalsIgnoreCase(COLOR_LOCATION)) setRoom(e.getText());
			else if (e.color.equalsIgnoreCase(COLOR_TIME)) parseTime(e.getText());
			else if (e.color.equalsIgnoreCase(COLOR_TYPE)) parseType(e.getText());
			else if (e.color.equalsIgnoreCase(COLOR_LECTURER)) lecturer = e.getText();
		}
		
		if (valid == true)
		{
			if (id == 0)
			{
				Log.e(logTag, "Event id is incorrect: "+id);
			}
			else if (allowCache==false)
			{
				if (loadAdditionalInfo(context, timetable)==false)
				{
					downloadAdditionalInfo(context, timetable);
				}
			}
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
	public void importFromString(String string, Timetable timetable)
	{
		if (TextUtils.isEmpty(string)) 
		{
			valid = false;
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
					addGroup(s, timetable);
				}
			}
			complete=true;
			decodeWeeks();
			
			valid=true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			valid = false;		
			Log.w(logTag, "Cannot parse event "+string);
		}
		valid=true;
	}
	
	public boolean isValid()
	{
		return valid;
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
			view = layoutInflater.inflate(R.layout.timetable_event_small, parent, false);
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
		
		return view;		
	}
}
