package com.mick88.dittimetable.timetable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import screens.EventDetailsActivity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mick88.dittimetable.Connection;
import com.mick88.dittimetable.R;


/*Holds information about a single event*/
public class TimetableEvent implements Comparable<TimetableEvent>
{	
	private final String htmlNbspChar = "\u00A0";	
	public enum ClassType {Other, Lecture, Laboratory, Tutorial};
	
	final String groupSeparator = ", ";
	Timetable timetable=null;
	final String logTag = "TimetableClass";
	final static String 
			color_name = "#987E06", //987E06 - how nice of them to not change colors
			color_group = "#987E06",
			color_type = "#E32198", //E32198
			color_location = "#062F98", //062F98
			color_lecturer = "#069810", //069810
			color_time = "#940AA8", //940AA8
			color_weeks = "#177F23";
			;
	
	/*Main event data*/
	private String name="", room="", lecturer = "", weeks="", groupStr="";
	private int id=0,
			 startHour=0,
				startMin=0,
				endMin=0,
			endHour=0;
	private ClassType type=ClassType.Other;
	ArrayList<String> groups = new ArrayList<String>();
	/**
	 * Stores parsed list of weeks when event is on
	 */
	List<Integer> weekList;
	
	boolean valid=true;
	
	String StripNbsp(String string)
	{
		return string.replaceAll(htmlNbspChar, "");
	}
	
	private String getFileName()
	{
		return String.format(Locale.getDefault(), "%d.html", id);
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
	
	public String getWeeks()
	{
		return weeks;
	}
	
	private String groupToString()
	{
		StringBuilder builder = new StringBuilder();
		int n=0;
		for (String g : groups)
		{
			if (n++ > 0) builder.append(groupSeparator);
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
	
	public boolean isEventOn(int hour)
	{
		return (hour >= startHour && hour < endHour);
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
		return week == 0 || weekList.isEmpty() || weekList.contains(week);
	}
	
	@Deprecated
	void shortenRoom()
	{
		/*StringBuilder builder = new StringBuilder();
		for (int i=0; i < room.length(); i++)
		{
			char c = room.charAt(i);
			if (c >= 'a' && c <= 'z') continue; //lower case
			if (c == ' ') continue;
			builder.append(c);
		}
		roomShort = builder.toString();*/
	}
	
	static final int ID_DAY = 2,
			ID_START = 3,
			ID_FINISH = 4,
			ID_ROOM = 5,
			ID_NAME = 8,
			ID_TYPE = 9;
	
	static final String colStart = "<td class=\"gridData\">",
			colEnd = "</td>";		
	
//	int duration; //duration of the class (hours)
	
/*	@Deprecated
	public TimetableEvent(String name, String room, int startTime, int endTime)
	{
		this.name = name;
		this.room = room;
		
		this.startTime = startTime;
		this.endTime = endTime;
		
		this.duration = endTime-startTime;
	}*/
	
	private TimetableEvent(Timetable timetable)
	{
		this.timetable = timetable;
		weekList = new ArrayList<Integer>();
	}
	
	/**
	 * Creates new object by parsing data and loading/downloading additional data
	 * @param table
	 * @param timetable
	 * @param context
	 */
	public TimetableEvent(Element table, Timetable timetable, Context context, boolean allowCache)
	{
		this(timetable);
		parseNewHtmlTable(table, context, allowCache);
//		this.duration = this.endTime - this.startTime;
	}
	
	/**
	 * Creates new object by importing data from string
	 * @param importString	exported string
	 * @param timetable	Timetable object
	 */
	public TimetableEvent(String importString, Timetable timetable)
	{
		this(timetable);
		importFromString(importString);
//		this.duration = this.endTime - this.startTime;
	}
	
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
	
	public boolean isGroup(List<String> hiddenGroups)
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
			timetable.addClassGroup(group);
			groupStr = groupToString();
		}
	}
	
	public String getGroupStr()
	{
		return groupStr;
	}
	
	/*private void parseGroups(String g)
	{
		String [] groupsArray = g.split(",");
		
//		String code = timetable.getCourseYearCode();
		
		for (String s : groupsArray)
		{
			Log.d(logTag, s);
			groups.add(s);
			timetable.addClassGroup(s);
		}
		
//		if (groups.size() > 0)
		{
			StringBuilder builder = new StringBuilder();
			int n=0;
			for (String s : groups)
			{
				if (n++ > 0) builder.append(',');
				builder.append(s);				
			}
			group = builder.toString();
		}
	}*/
	
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
	private void downloadAdditionalInfo(Context context)
	{
		Connection connection = timetable.getConnection();
		String uri = String.format(Locale.getDefault(), "?reqtype=eventdetails&eventId=%s%%7C%d", Timetable.getDataset(), id);

		String content = connection.getContent(uri);
		if (parseAdditionalInfo(content))
		{
			/*try
			{
				saveAdditionalInfo(context, content);
			} catch (Exception e)
			{
				e.printStackTrace();
			}*/
		}
	}
	
	@Deprecated
	private boolean loadAdditionalInfo(Context context)
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
		
		Log.i(logTag, "Event loaded from "+filename);
		
		if (parseAdditionalInfo(sb.toString()))
		{
			Log.i(logTag, "Timetable successfully loaded from file");
			return true;
		}
		return false;
	}
	
	@Deprecated
	private void saveAdditionalInfo(Context context, String content) throws IOException
	{
		String filename = getFileName();
		FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);			
		byte[] buffer = content.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
		Log.i(logTag, "Timetable saved to "+filename);
	}
	
	/**
	 * parses the additional info page
	 */
	private boolean parseAdditionalInfo(String content)
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
	
				addGroup(group);
			}
			weeks = tableValues.get("Week numbers");
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
	public void parseNewHtmlTable(Element table, Context context, boolean allowCache)
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
			//valid=false;
		}
			
		Elements elements = table.select("td");
		for (Element element : elements)
		{
			EventElement e = new EventElement(element);
			if (e.color.equalsIgnoreCase(color_name))
			{
				if (TextUtils.isEmpty(name)) name = e.getText();
			}
			else if (e.color.equalsIgnoreCase(color_location)) setRoom(e.getText());
			else if (e.color.equalsIgnoreCase(color_time)) parseTime(e.getText());
			else if (e.color.equalsIgnoreCase(color_type)) parseType(e.getText());
			else if (e.color.equalsIgnoreCase(color_lecturer)) lecturer = e.getText();
		}
		
		if (valid == true)
		{
			if (id == 0)
			{
				Log.e(logTag, "Event id is incorrect: "+id);
			}
			else if (allowCache==false || loadAdditionalInfo(context) == false)
			{
				downloadAdditionalInfo(context);
			}
			//valid = (TextUtils.isEmpty(name) == false) && (TextUtils.isEmpty(room) == false) && (startHour > 0) && (endHour > 0);
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
		
		builder.append(weeks).append(exportItemSeparator);
		builder.append(type.name()).append(exportItemSeparator);
		builder.append(groupToString().replace(exportItemSeparator, "")).append(exportItemSeparator);		
		
		return builder.toString();
	}
	
	private void decodeWeeks()
	{
		// TODO: implement decode string weeks to list
		weekList = new ArrayList<Integer>();
		String [] sections = weeks.split(",");
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
						weekList.add(min++);
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
					weekList.add(week);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(logTag, "Cannot parse week "+sectionTrimmed);
				}
			}
		}
		Log.d(logTag, "Weeks decoded to "+weekList.size());
	}
	
	public void importFromString(String string)
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
			
			weeks = fields[field++];
			type = ClassType.valueOf(fields[field++]);
			
			if (fields.length >= field+1)
			{
				String [] g = fields[field++].split(groupSeparator);
				for (String s : g)
				{
					addGroup(s);
				}
			}
			
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
	
	public View getTile(final Context context, boolean small)
	{
		ViewGroup tile = (ViewGroup) LayoutInflater.from(context).inflate(small?R.layout.timetable_event_tiny:R.layout.timetable_event_small, null);
		
		((TextView) tile.findViewById(R.id.eventTitle)).setText(this.getName());
		((TextView) tile.findViewById(R.id.eventTime)).setText(String.format(Locale.getDefault(), "%s - %s", this.getStartTime(), this.getEndTime()));
		((TextView) tile.findViewById(R.id.eventLocation)).setText(this.getRoomShort());
		
		TextView textGroup = (TextView) tile.findViewById(R.id.eventGroup);
		if (TextUtils.isEmpty(this.getGroupStr()))
		{
			textGroup.setVisibility(View.GONE);
		}
		else
		{
			textGroup.setText(this.getGroupStr());
			textGroup.setVisibility(View.VISIBLE);
		}
		
		TextView textLecturer = (TextView) tile.findViewById(R.id.eventLecturer);
		if (textLecturer != null)
		{
			textLecturer.setText(this.getLecturer());
		}
		
		/*Set type and its color*/
		TextView textEventType = (TextView) tile.findViewById(R.id.eventType);
		ClassType type = this.getClassType();
		int color = 0x000000;				
		switch (type)
		{
			case Lecture:
				color = context.getResources().getColor(R.color.color_lecture);
				break;
			case Laboratory:
				color = context.getResources().getColor(R.color.color_laboratory);
				break;
			case Tutorial:
				color = context.getResources().getColor(R.color.color_tutorial);
				break;
			default:
				color = 0xFFFFFF;
				break;
		}
		textEventType.setTextColor(color);
		textEventType.setText(type.toString());
		
		View.OnClickListener eventClickedListener = new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context.getApplicationContext(), EventDetailsActivity.class);
				intent.putExtra("event_id", TimetableEvent.this.getId());
				intent.putExtra("name", TimetableEvent.this.getName());
				intent.putExtra("room", TimetableEvent.this.getRoom());
				intent.putExtra("lecturer", TimetableEvent.this.getLecturer());
				intent.putExtra("startTime", TimetableEvent.this.getStartTime());
				intent.putExtra("endTime", TimetableEvent.this.getEndTime());
				intent.putExtra("weeks", TimetableEvent.this.getWeeks());
				intent.putExtra("type", TimetableEvent.this.getType().toString());
				intent.putExtra("groups", TimetableEvent.this.getGroupStr());
				context.startActivity(intent);
			}
		};
		// set onclick listener
		
		if (small == false)
			((RelativeLayout) tile.findViewById(R.id.timetable_event_small)).setOnClickListener(eventClickedListener);
		else
			((LinearLayout) tile.findViewById(R.id.timetable_event_tiny)).setOnClickListener(eventClickedListener);
		
		return tile;
	}

	@Override
	public int compareTo(TimetableEvent another)
	{
		if (another.startHour > this.startHour) return -1;
		else if (another.startHour < this.startHour) return 1;
		
		return groupStr.compareTo(another.groupStr);
	}
}
