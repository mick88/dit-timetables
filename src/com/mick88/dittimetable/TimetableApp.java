package com.mick88.dittimetable;

import java.util.HashMap;
import java.util.Map;

import com.mick88.dittimetable.settings.AppSettings;

import android.app.Application;
import android.os.Build;

public class TimetableApp extends Application
{
	private AppSettings settings;
	public static final String FLURRY_API_KEY = "DN7DRPJHSB5WX5FSX5SG";
	public static final String FONT_NAME = "Roboto-Light.ttf";
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		settings = new AppSettings(getApplicationContext(), true);
	}
	
	public AppSettings getSettings()
	{
		return settings;
	}
	
	public static Map<String,String> getDeviceData()
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("Brand", Build.BRAND);
		result.put("Device", Build.DEVICE);
		result.put("Android release", Build.VERSION.RELEASE);
		result.put("Android SDK", Integer.toString(Build.VERSION.SDK_INT));
		
		return result;
	}
}
