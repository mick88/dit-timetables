package com.mick88.dittimetable.widget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.TimetableActivity;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TimetableWidget extends AppWidgetProvider
{
	private static final String LOG_TAG = "WidgetProvider";
	private PendingIntent pendingUpdateIntent=null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		RemoteViews views = buildViews(context);
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		Log.d(LOG_TAG, "Widget updated");
		
		setRefreshTimer(context);
	}
	
	void setRefreshTimer(Context context)
	{
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if (pendingUpdateIntent == null)
		{
			final Intent updateServiceIntent = new Intent(context, TimetableUpdateService.class);
			this.pendingUpdateIntent = PendingIntent.getService(context, 0, updateServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		}
		
		// Setup auto-refresh		
		final Calendar time = Calendar.getInstance();
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		time.add(Calendar.HOUR, 1);
		
		alarmManager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), TimetableUpdateService.UPDATE_INTERVAL, this.pendingUpdateIntent);
	}
	
	RemoteViews buildViews(Context context)
	{
		AppSettings settings = AppSettings.loadFromPreferences(context);
		Timetable timetable = new DatabaseHelper(context).getTimetable(settings.getCourse(), settings.getYear(), settings.getWeekRange());
		int day = Timetable.getTodayId(true);
		
		RemoteViews result = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		result.setEmptyView(R.id.listEvents, R.id.empty);
		if (timetable == null)
		{
			// timetable not downloaded
			result.setTextViewText(R.id.empty, context.getString(R.string.please_download_timetable));
		}
		else
		{
			Intent rvServiceIntent = new Intent(context.getApplicationContext(), RemoteTimetableAdapter.class);
			rvServiceIntent.putExtra(RemoteTimetableAdapter.EXTRA_COURSE_CODE, timetable.getCourse());
			result.setRemoteAdapter(R.id.listEvents, rvServiceIntent);
			
			result.setTextViewText(R.id.tvWidgetCourseCode, new StringBuilder(timetable.describe()).append(' ').append(timetable.describeWeeks()));
		}
		Intent timetableIntent = new Intent(context, TimetableActivity.class);
		//timetableIntent.putExtra(TimetableActivity.EXTRA_SHOW_DAY_ID, day);
		result.setOnClickPendingIntent(R.id.widget_header, PendingIntent.getActivity(context, 0, timetableIntent, 0));		
		
		result.setTextViewText(R.id.tvWidgetDate, Timetable.DAY_NAMES[day]);
		
		return result;		
	}
	
	@Override
	public void onDisabled(Context context)
	{
		super.onDisabled(context); 
		
		if (pendingUpdateIntent != null) 
		{
			final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingUpdateIntent);
		}
	}
	
	public static List<TimetableEvent> getTodaysRemainingEvents(Context context, Timetable timetable, AppSettings settings)
	{
		TimetableDay day = timetable.getToday(true);
		List<TimetableEvent> events = new ArrayList<TimetableEvent>(day.getEvents(settings));
		
		if (day.isToday())
		{
			// remove passed events
			final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			for (int i=events.size()-1; i >= 0; i--)
			{
				if (events.get(i).getEndHour() <= hour)
					events.remove(i);
			}
		}
		
		return events;
	}
}
