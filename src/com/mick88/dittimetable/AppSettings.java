package com.mick88.dittimetable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class AppSettings implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private static final String 
		DEFAULT_USERNAME = "students",
		DEFAULT_PASSWORD = "timetables";
	public static final String PREFERENCES_NAME = "com.mick88.dittimetable";
	public static final String GROUP_SEPARATOR = ",";
	
	String username, 
		password;	
	String
		course, weeks;
	int year;
	boolean onlyCurrentWeek;
	Set<String> hiddenGroups;
	
	public AppSettings()
	{		
		username = new String();
		password = new String();
		
		course = new String();
		weeks = new String();
		year = 0;
		
		onlyCurrentWeek = false;
		hiddenGroups = new HashSet<String>();
	}
	
	public boolean isCourseDataSpecified()
	{
		return TextUtils.isEmpty(course) == false && TextUtils.isEmpty(weeks) == false && year > 0;
	}
	
	public AppSettings(Context context, boolean loadSettings)
	{
		this();
		if (loadSettings == true) this.loadSettings(context);
	}
	
	public void loadSettings(Context context)
	{
		loadSettings(context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE));
	}
	
	public void loadSettings(SharedPreferences sharedPreferences)
	{
		username = sharedPreferences.getString("username", DEFAULT_USERNAME);
		password = sharedPreferences.getString("password", DEFAULT_PASSWORD);
		
		course = sharedPreferences.getString("course", "");
		weeks = sharedPreferences.getString("weeks", "");
		year = sharedPreferences.getInt("year", 0);
		
		onlyCurrentWeek = sharedPreferences.getBoolean("only_current_week", false);
		
		setHiddenGroupsString(sharedPreferences.getString("hidden_groups", ""));
		
		Log.i(toString(), "Settings loaded");
	}
	
	public void saveSettings(Context context)
	{
		saveSettings(context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE));
	}
	
	public void saveSettings(SharedPreferences sharedPreferences)
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
		this.hiddenGroups = new HashSet<String>(groups.length);
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
	
	public Set<String> getHiddenGroups()
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
