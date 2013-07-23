package com.mick88.dittimetable;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class AppSettings implements OnSharedPreferenceChangeListener
{
	SharedPreferences sharedPreferences;
	public static final String sharedPrefNameTag = "com.mick88.dittimetable";
	final static String GROUP_SEPARATOR = ",";
	
	String username, 
		password;	
	String
		course, weeks;
	int year;
	boolean onlyCurrentWeek;
	List<String> hiddenGroups;
	
	public AppSettings(Context context)
	{
		sharedPreferences = context.getSharedPreferences(sharedPrefNameTag, Context.MODE_PRIVATE);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		username = new String();
		password = new String();
		
		course = new String();
		weeks = new String();
		year = 0;
		
		onlyCurrentWeek = false;
		hiddenGroups = new ArrayList<String>();
	}
	
	public AppSettings(Context context, boolean loadSettings)
	{
		this(context);
		if (loadSettings == true) this.loadSettings();
	}
	
	public void loadSettings()
	{
		username = sharedPreferences.getString("username", "");
		password = sharedPreferences.getString("password", "");
		
		course = sharedPreferences.getString("course", "");
		weeks = sharedPreferences.getString("weeks", "");
		year = sharedPreferences.getInt("year", 0);
		
		onlyCurrentWeek = sharedPreferences.getBoolean("only_current_week", false);
		
		setHiddenGroupsString(sharedPreferences.getString("hidden_groups", ""));
		
		Log.i(toString(), "Settings loaded");
	}
	
	public void saveSettings()
	{
		sharedPreferences.edit()
			.putString("username", username)
			.putString("password", password)
			
			.putString("course", course)
			.putString("weeks", weeks)
			.putInt("year", year)
			
			.putString("hidden_groups", getHiddenGroupsString())
			.putBoolean("only_current_week", onlyCurrentWeek)
			
			.commit();
		Log.i(toString(), "App settings saved");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		Log.d(toString(), "Shared preference key value changed: "+key);
		
	}
	
	private String getHiddenGroupsString()
	{
		StringBuilder builder = new StringBuilder();
		for (String group : hiddenGroups)
		{
			builder.append(group).append(GROUP_SEPARATOR);
		}
		return builder.toString();
	}
	
	/**
	 * Sets hidden groups and clears previous values
	 */
	private void setHiddenGroupsString(String groupString)
	{
		String [] groups = groupString.split(GROUP_SEPARATOR);
		this.hiddenGroups = new ArrayList<String>(groups.length);
		for (String group : groups)
		{
			this.hiddenGroups.add(group);
		}
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getCourse()
	{
		return course;
	}
	
	public int getYear()
	{
		return year;
	}
	
	/**
	 * defines whether user prefers to see only events for the current week
	 * @return
	 */
	public boolean getOnlyCurrentWeek()
	{
		return onlyCurrentWeek;
	}
	
	public String getWeeks()
	{
		return weeks;
	}
	
	public void setCourse(String course)
	{
		this.course = course;
	}
	
	public void setYear(int year)
	{
		this.year = year;
	}
	
	public void setWeeks(String weeks)
	{
		this.weeks = weeks;
	}
	
	public List<String> getHiddenGroups()
	{
		return hiddenGroups;
	}

	public void setOnlyCurrentWeek(boolean onlyCurrentWeek)
	{
		this.onlyCurrentWeek = onlyCurrentWeek;
	}
	
	
	
	public void hideGroup(String groupCode)
	{
//		if (groupCode.equals(getCourseYearCode())) return; //cannot hide whole class
		if (hiddenGroups.contains(groupCode) == false)
		{
			hiddenGroups.add(groupCode);
		}	
	}
	
	public void unhideGroup(String group)
	{
		hiddenGroups.remove(group);
	}
}
