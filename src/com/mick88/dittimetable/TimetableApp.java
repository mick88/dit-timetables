package com.mick88.dittimetable;

import android.app.Application;

public class TimetableApp extends Application
{
	private AppSettings settings;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		settings = new AppSettings(getApplicationContext(), true);
		settings.loadSettings();
	}
	
	public AppSettings getSettings()
	{
		return settings;
	}
}
