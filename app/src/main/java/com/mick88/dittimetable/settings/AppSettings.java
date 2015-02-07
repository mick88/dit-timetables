package com.mick88.dittimetable.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class AppSettings implements Serializable
{
    private final SharedPreferences preferences;
	private static final long serialVersionUID = 1L;
	
	public static final String
            PREF_ONLY_CURRENT_WEEK = "only_current_week",
	        PREF_HIDDEN_GROUPS = "hidden_groups",
	        PREF_HIDDEN_GROUPS_SET = "hidden_groups_set",
	        PREF_HIDDEN_MODULES = "hidden_modules",
	        PREF_HIDDEN_MODULES_SET = "hidden_modules_set",
	        PREF_YEAR = "year",
	        PREF_WEEKS = "weeks",
	        PREF_COURSE = "course",
	        PREF_PASSWORD = "password",
	        PREF_USERNAME = "username",
	        PREF_EVENT_NOTIFICATIONS = "event_notifications",
	        PREF_LAST_NOTIFIED_TIMESLOT = "last_notified";

	private static final String 
		DEFAULT_USERNAME = "students",
		DEFAULT_PASSWORD = "timetables";
	public static final String PREFERENCES_NAME = "com.mick88.dittimetable";
	public static final String GROUP_SEPARATOR = ",";
	private static final String MODULE_SEPARATOR = "|";

	public AppSettings(SharedPreferences sharedPreferences)
	{
        this.preferences = sharedPreferences;
	}

    public AppSettings(Context context)
    {
        this(context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE));
    }

	public boolean isCourseDataSpecified()
	{
        return preferences.contains(PREF_COURSE)
                && preferences.contains(PREF_WEEKS)
                && preferences.contains(PREF_YEAR);
	}
	
	public String getPassword()
	{
		return preferences.getString(PREF_PASSWORD, DEFAULT_PASSWORD);
	}
	
	public String getUsername()
	{
		return preferences.getString(PREF_USERNAME, DEFAULT_USERNAME);
	}
	
	public void setPassword(String password)
	{
		preferences.edit().putString(PREF_PASSWORD, password).apply();
	}
	
	public void setUsername(String username)
	{
        preferences.edit().putString(PREF_USERNAME, username).apply();
	}
	
	public String getCourse()
	{
        return preferences.getString(PREF_COURSE, "");
	}
	
	public int getYear()
	{
		return preferences.getInt(PREF_YEAR, 1);
	}
	
	/**
	 * defines whether user prefers to see only events for the current week
	 * @return
	 */
	public boolean getOnlyCurrentWeek()
	{
		return preferences.getBoolean(PREF_ONLY_CURRENT_WEEK, false);
	}
	
	public String getWeekRange()
	{
		return preferences.getString(PREF_WEEKS, "");
	}
	
	public void setCourse(String course)
	{
		preferences.edit().putString(PREF_COURSE, course).apply();
	}
	
	public void setYear(int year)
	{
		preferences.edit().putInt(PREF_YEAR, year).apply();
	}
	
	public void setWeekRange(String weekRange)
	{
		preferences.edit().putString(PREF_WEEKS, weekRange).apply();
	}
	
	public Set<String> getHiddenGroups()
	{
		if (preferences.contains(PREF_HIDDEN_GROUPS_SET))
            return preferences.getStringSet(PREF_HIDDEN_GROUPS_SET, Collections.<String>emptySet());
        else
        {
            String string = preferences.getString(PREF_HIDDEN_GROUPS, "");
            return new HashSet<>(Arrays.asList(string.split(GROUP_SEPARATOR)));
        }
	}

	public void setOnlyCurrentWeek(boolean onlyCurrentWeek)
	{
		preferences.edit().putBoolean(PREF_ONLY_CURRENT_WEEK, onlyCurrentWeek).apply();
	}
	
	public void hideGroup(String groupCode)
	{
        Set<String> hiddenGroups = getHiddenGroups();
        hiddenGroups.add(groupCode);
        setHiddenGroups(hiddenGroups);
	}
	
	public void unhideGroup(String group)
	{
        Set<String> hiddenGroups = getHiddenGroups();
		hiddenGroups.remove(group);
        setHiddenGroups(hiddenGroups);
	}

	public Set<String> getHiddenModules()
	{
        if (preferences.contains(PREF_HIDDEN_MODULES_SET))
            return preferences.getStringSet(PREF_HIDDEN_MODULES_SET, Collections.<String>emptySet());
        else
        {
            String string = preferences.getString(PREF_HIDDEN_MODULES, "");
            return new HashSet<>(Arrays.asList(string.split(Pattern.quote(MODULE_SEPARATOR))));
        }
	}

	public void unhideModule(String s)
	{
        Set<String> hiddenModules = getHiddenModules();
        hiddenModules.remove(s);
        setHiddenModules(hiddenModules);
	}

    private void setHiddenModules(Set<String> hiddenModules)
    {
        preferences.edit().putStringSet(PREF_HIDDEN_MODULES_SET, hiddenModules).apply();
    }

    private void setHiddenGroups(Set<String> hiddenGroups)
    {
        preferences.edit().putStringSet(PREF_HIDDEN_GROUPS_SET, hiddenGroups).apply();
    }

    public void hideModule(String s)
	{
        Set<String> hiddenModules = getHiddenModules();
		hiddenModules.add(s);
        setHiddenModules(hiddenModules);
		
	}

    public boolean getEventNotifications()
    {
        return preferences.getBoolean(PREF_EVENT_NOTIFICATIONS, true);
    }

    public void setEventNotifications(boolean eventNotifications)
    {
        preferences.edit().putBoolean(PREF_EVENT_NOTIFICATIONS, eventNotifications).apply();
    }

    /**
     * Check if user was last notified about events for this time and day
     * if not, set this time/day as last notified
     */
    public boolean wasTimeslotNotified(int day, int timeslot)
    {
        final String notifiedTimeslot = String.format(Locale.ENGLISH, "%d%d", day, timeslot);

        if (preferences.getString(PREF_LAST_NOTIFIED_TIMESLOT, "").equals(notifiedTimeslot)) return true;
        else
        {
            preferences.edit().putString(PREF_LAST_NOTIFIED_TIMESLOT, notifiedTimeslot).apply();
            return false;
        }
    }


}
