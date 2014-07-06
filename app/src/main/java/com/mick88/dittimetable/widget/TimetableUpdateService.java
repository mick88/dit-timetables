package com.mick88.dittimetable.widget;

import android.annotation.TargetApi;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.R.id;

public class TimetableUpdateService extends Service
{
	private static final String LOG_TAG = "TimetableUpdateService";
	public static final long UPDATE_INTERVAL = 1000 * 60 * 60;
	
	@Override
	public IBinder onBind(Intent intent)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		refreshWidget(getApplicationContext());
		return START_NOT_STICKY;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void refreshWidget(Context context)
	{
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		int [] ids = widgetManager.getAppWidgetIds(new ComponentName(context, TimetableWidget.class));
		if (ids.length > 0)
		{
			widgetManager.notifyAppWidgetViewDataChanged(ids, R.id.listEvents);
			new TimetableWidget().onUpdate(context, widgetManager, ids);
			Log.d(LOG_TAG, "Widget updated");
		}
	}
}
