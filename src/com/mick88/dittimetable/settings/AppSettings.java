package com.mick88.dittimetable.settings;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class AppSettings implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private static final String PREF_ONLY_CURRENT_WEEK = "only_current_week";
	private static final String PREF_HIDDEN_GROUPS = "hidden_groups";
	private static final String PREF_HIDDEN_MODULES = "hidden_modules";
	private static final String PREF_YEAR = "year";
	private static final String PREF_WEEKS = "weeks";
	private static final String PREF_COURSE = "course";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_USERNAME = "username";
	
	private static final String 
		DEFAULT_USERNAME = "students",
		DEFAULT_PASSWORD = "timetables";
	public static final String PREFERENCES_NAME = "com.mick88.dittimetable";
	public static final String GROUP_SEPARATOR = ",";

	private static final String MODULE_SEPARATOR = "|";
	
	String username, 
		password;	
	String
		course, weekRange;
	int year;
	boolean onlyCurrentWeek;
	Set<String> hiddenGroups;
	Set<String> hiddenModules;
	
	public AppSettings()
	{		
		username = new String();
		password = new String();
		
		course = new String();
		weekRange = new String();
		year = 0;
		
		onlyCurrentWeek = false;
		hiddenGroups = new HashSet<String>();
		hiddenModules = new HashSet<String>();
	}
	
	public boolean isCourseDataSpecified()
	{
		return TextUtils.isEmpty(course) == false && TextUtils.isEmpty(weekRange) == false && year > 0;
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
		username = sharedPreferences.getString(PREF_USERNAME, DEFAULT_USERNAME);
		password = sharedPreferences.getString(PREF_PASSWORD, DEFAULT_PASSWORD);
		
		course = sharedPreferences.getString(PREF_COURSE, "");
		weekRange = sharedPreferences.getString(PREF_WEEKS, "");
		year = sharedPreferences.getInt(PREF_YEAR, 0);
		
		onlyCurrentWeek = sharedPreferences.getBoolean(PREF_ONLY_CURRENT_WEEK, false);
		
		setHiddenGroups(sharedPreferences.getString(PREF_HIDDEN_GROUPS, ""));
		setHiddenModules(sharedPreferences.getString(PREF_HIDDEN_MODULES, ""));
		
		Log.i(toString(), "Settings loaded");
	}
	
	public void saveSettings(Context context)
	{
		saveSettings(context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE));
	}
	
	public void saveSettings(SharedPreferences sharedPreferences)
	{
		sharedPreferences.edit()
			.putString(PREF_USERNAME, username)
			.putString(PREF_PASSWORD, password)
			
			.putString(PREF_COURSE, course)
			.putString(PREF_WEEKS, weekRange)
			.putInt(PREF_YEAR, year)
			
			.putString(PREF_HIDDEN_GROUPS, getHiddenGroupsString())
			.putBoolean(PREF_ONLY_CURRENT_WEEK, onlyCurrentWeek)
			.putString(PREF_HIDDEN_MODULES, getHiddenModulesString())
			
			.commit();
		Log.i(toString(), "App settings saved");
	}
	
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "%s-%d %s", course, year, weekRange);
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
	
	private String getHiddenModulesString()
	{
		StringBuilder builder = new StringBuilder();
		for (String module : hiddenModules)
		{
			builder.append(module).append(MODULE_SEPARATOR);
		}
		return builder.toString();
	}
	
	/**
	 * Sets hidden groups and clears previous values
	 */
	private void setHiddenGroups(String groupString)
	{
		String [] groups = groupString.split(GROUP_SEPARATOR);
		hiddenGroups.clear();
		hiddenGroups.addAll(Arrays.asList(groups));
	}
	
	private void setHiddenModules(String moduleString)
	{
		String [] modules = moduleString.split(Pattern.quote(MODULE_SEPARATOR));
		hiddenModules.clear();
		hiddenModules.addAll(Arrays.asList(modules));
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
	
	public String getWeekRange()
	{
		return weekRange;
	}
	
	public void setCourse(String course)
	{
		this.course = course;
	}
	
	public void setYear(int year)
	{
		this.year = year;
	}
	
	public void setWeekRange(String weekRange)
	{
		this.weekRange = weekRange;
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
		hiddenGroups.add(groupCode);
	}
	
	public void unhideGroup(String group)
	{
		hiddenGroups.remove(group);
	}

	public Set<String> getHiddenModules()
	{
		return hiddenModules;
	}

	public void unhideModule(String s)
	{
		hiddenModules.remove(s);
	}

	public void hideModule(String s)
	{
		hiddenModules.add(s);
		
	}
}
