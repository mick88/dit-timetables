package com.mick88.dittimetable.timetable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.list.MultiEvent;
import com.mick88.dittimetable.list.Space;

/**
 * ontains list of classes in a day
 * 
 */
public class TimetableDay
{
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
			TimetableEvent event = new TimetableEvent(eventString, timetable);
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
	
	@Deprecated
	public View getView(LayoutInflater inflater, Context context)
	{
		View view = inflater.inflate(R.layout.day_layout, null);
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.dayContent);
		
		drawTimetable(layout, inflater, context);
//		Log.d(toString(), "View created");
		return view;
	}
	
	private static View getSpacer(LayoutInflater inflater, int num, int highligh, Context context)
	{
		ViewGroup space = (ViewGroup) inflater.inflate(R.layout.timetable_event_empty, null);
		ViewGroup container = (ViewGroup) space.findViewById(R.id.separator_dot_container);
		for (int i=0; i < num; i++)
		{
			ImageView imageView = new ImageView(context);
			imageView.setImageResource((i == highligh)?R.drawable.dot_selected:R.drawable.dot);
			imageView.setPadding(5,5,5,5);
			
			container.addView(imageView);
		}
		return space;
	}
	
	public List<EventItem> getTimetableEntries()
	{
		List<EventItem> entries = new ArrayList<EventItem>(events.size());
		
		int lastEndHour=0;
		TimetableEvent lastEvent=null;
		
		AppSettings settings = timetable.getSettings();
		int currentWeek = Timetable.getCurrentWeek();
		List<TimetableEvent> sameHourEvents = new ArrayList<TimetableEvent>();
		
		synchronized (events)
		{
			for (TimetableEvent event : events)
			{
				if (lastEvent != null)
				{
					// add space if there was a time off between the events
					if (lastEndHour < event.getStartHour())
					{
						entries.add(new Space(event.getStartHour() - lastEndHour, lastEndHour));
					}
				}
				
				int numEvents = getNumEvents(event.getStartHour(), settings.getHiddenGroups(), 0);
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
	
	@Deprecated
	void drawTimetable(ViewGroup parentLayout, LayoutInflater inflater, Context context)
	{		
		int hour=0;
		boolean isToday = isToday();
		if (isToday)
		{
			hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		}
		
		ViewGroup hsViews[] = new HorizontalScrollView[] {
			null, null, null, null, null, null, 
			null, null, null, null, null, null,
			null, null, null, null, null, null, 
			null, null, null, null, null, null};
		
		int lastEndHour=0;
		TimetableEvent currentEvent=null;
		
		AppSettings settings = timetable.getSettings();
		int currentWeek = Timetable.getCurrentWeek();
		
		int showWeek = settings.getOnlyCurrentWeek()?currentWeek : 0;
		
		/*TimetableActivity activity = (TimetableActivity) timetable.getParentActivity();
		ViewPager viewPager = activity.getViewPager();*/
		
		
		synchronized (events)
		{
			for (TimetableEvent event : events) 
				if (event.isGroup(settings.getHiddenGroups()) && event.isInWeek(settings.getOnlyCurrentWeek() ? currentWeek:0))
			{			
				int numClassesAtCurrentHour = getNumEvents(event.getStartHour(), settings.getHiddenGroups(), showWeek);
				boolean isSingleEvent = numClassesAtCurrentHour == 1;
				View tile = event.getTile(context, isSingleEvent == false, inflater); 
				
				// mark current event
				if ((isToday && currentEvent == null && event.getEndHour() > hour)
						|| (currentEvent != null && (event.getStartHour() == currentEvent.getStartHour())))
				{
					currentEvent = event;
					int rDrawable = event.isEventOn(hour)?R.drawable.event_selected_selector:R.drawable.event_upcoming_selector;
					
					if (isSingleEvent)
						((RelativeLayout) tile.findViewById(R.id.timetable_event_small)).setBackgroundResource(rDrawable);
					else
						((LinearLayout) tile.findViewById(R.id.timetable_event_tiny)).setBackgroundResource(rDrawable);
					
				}
				
				if (lastEndHour > 0)
				{
					int hours = event.getStartHour()-lastEndHour;
					if (hours > 0) 
					{
						parentLayout.addView(getSpacer(inflater, hours, isToday?(hour-lastEndHour):(-1), context));
					}
				}
				lastEndHour=event.getEndHour();
				
				/*Add to layout*/
				if (isSingleEvent) 
				{
					parentLayout.addView(tile);
				}
				else
				{
					int startTime = event.getStartHour();
					ViewGroup multiEventContainer = hsViews[startTime];
					
					// create scroller if doesnt exist
					if (multiEventContainer == null)
					{
						multiEventContainer = new HorizontalScrollView(context);

						((HorizontalScrollView) multiEventContainer).setFillViewport(true);
						
						LayoutParams sameHourContainerParams = new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
						multiEventContainer.setLayoutParams(sameHourContainerParams);
		
						ViewGroup host = new LinearLayout(context);
						host.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
										
						multiEventContainer.addView(host);
		//					sameHourEventContainer.setScrollbarFadingEnabled(false);
						
						hsViews[startTime] = multiEventContainer;
						parentLayout.addView(multiEventContainer);
					}
					
					// casting container's inner layout
					((ViewGroup) multiEventContainer.getChildAt(0)).addView(tile);
				}
			}
		}
	}
	
	public void downloadAdditionalInfo(Context context)
	{
		for (TimetableEvent event : events) if (event.isComplete() == false)
		{
			if (timetable.isDisposed()) break;
			event.downloadAdditionalInfo(context);
		}
	}
}
