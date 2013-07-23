package com.mick88.dittimetable.timetable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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

/*Contains list of classes in a day*/
public class TimetableDay
{
	Timetable timetable=null;
	String name = "";
	int id=-1;
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
	}
	
	public void sortEvents()
	{
		synchronized (classes)
		{
			Collections.sort(classes);
		}		
	}
	
	public void addClass(TimetableEvent c)
	{
		synchronized (classes)
		{
			classes.add(c);
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
	
/*	@Deprecated
	public int getNumClassesAt(int hour, int week)
	{
		return getNumClasses(hour, timetable.getHiddenGroups(), week);
	}*/
	
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
	
	public boolean isToday()
	{
		return timetable.getToday(false) == this;
	}
	
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
			imageView.setImageResource((i == highligh)?R.drawable.separator_dot_selected:R.drawable.separator_dot);
			imageView.setPadding(5,5,5,5);
			
			container.addView(imageView);
		}
		return space;
	}
	
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
		TimetableEvent selectedEvent=null;
		
		AppSettings settings = timetable.getSettings();
		int currentWeek = Timetable.getCurrentWeek();
		
		int showWeek = settings.getOnlyCurrentWeek()?currentWeek : 0;
		
		/*TimetableActivity activity = (TimetableActivity) timetable.getParentActivity();
		ViewPager viewPager = activity.getViewPager();*/
		
		
		synchronized (classes)
		{
			for (TimetableEvent event : classes) 
				if (event.isGroup(settings.getHiddenGroups()) && event.isInWeek(settings.getOnlyCurrentWeek() ? currentWeek:0))
			{			
				int numClassesAtCurrentHour = this.getNumClasses(event.getStartHour(), settings.getHiddenGroups(), showWeek);
				boolean isSingleEvent = numClassesAtCurrentHour == 1;
				View tile = event.getTile(context, isSingleEvent == false, inflater); 
				
				// mark current event
				if ((isToday && selectedEvent == null && event.getEndHour() > hour)
						|| (selectedEvent != null && (event.getStartHour() == selectedEvent.getStartHour())))
				{
					selectedEvent = event;
					int rDrawable = event.isEventOn(hour)?R.drawable.selected_item_selector:R.drawable.upcoming_item_selector;
					
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
	
	public void downloadAccitionalInfo(Context context)
	{
		for (TimetableEvent event : classes) if (event.isComplete() == false)
		{
			if (timetable.isDisposed()) break;
			event.downloadAdditionalInfo(context);
		}
	}
}
