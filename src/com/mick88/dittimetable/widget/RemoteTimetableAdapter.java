package com.mick88.dittimetable.widget;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableEvent;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RemoteTimetableAdapter extends RemoteViewsService implements RemoteViewsFactory
{
	public static final String 
		EXTRA_COURSE_CODE = "course_code";
	
	private List<TimetableEvent> events;
	private String courseCode=null;

	@Override
	public int getCount()
	{
		return events.size();
	}

	@Override
	public long getItemId(int position)
	{
		return events.get(position).getId();
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// TODO Auto-generated method stub
		return null;
	}
 
	@Override
	public RemoteViews getViewAt(int position)
	{
		RemoteViews view = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_item);
		TimetableEvent event = events.get(position);
		
		final String groups;
		if (TextUtils.isEmpty(courseCode))
			groups = event.getGroupStr();
		else groups = event.getGroupsString(courseCode);
		view.setTextViewText(R.id.eventGroup, groups);
		view.setTextViewText(R.id.eventLocation, event.getRoom());
		view.setTextViewText(R.id.eventTime, event.getEventTimeString().toString());
		view.setTextViewText(R.id.eventTitle, event.getName());
		view.setTextViewText(R.id.eventType, event.getEventType().toString());
		
		if (event.isEventOn())
		{
			view.setInt(android.R.id.content, "setBackgroundResource", R.drawable.widget_item_active_selector);
		}
		
		return view;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}
	
	

	@Override
	public void onCreate()
	{
		AppSettings settings = AppSettings.loadFromPreferences(getApplicationContext());
		Timetable timetable = new DatabaseHelper(getApplicationContext()).getTimetable(settings.getCourse(), settings.getYear(), settings.getWeekRange());
		this.events = TimetableWidget.getTodaysRemainingEvents(getApplicationContext(), timetable, settings);
	}

	@Override
	public void onDataSetChanged()
	{
		onCreate();
		
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		this.courseCode = intent.getStringExtra(EXTRA_COURSE_CODE);
		return this;
	}

	
}