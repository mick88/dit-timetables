package com.mick88.dittimetable.timetable;

import android.content.Context;
import android.text.TextUtils;

import com.michaldabski.msqlite.Annotations.PrimaryKey;
import com.michaldabski.msqlite.Annotations.TableName;
import com.mick88.dittimetable.settings.AppSettings;

import java.io.Serializable;
import java.util.Locale;

/**
 * Base class for Timetable
 * stores basic info: course, year, week range
 * @author Michal
 *
 */
@TableName("Timetable")
public class TimetableStub implements Serializable, Comparable<TimetableStub>
{
	private static final long serialVersionUID = 1L;
	
	public static final String 
		SEMESTER_1 = "4-20",
		SEMESTER_2  = "23-30,33-37",
		ALL_WEEKS  = "1-52";
	
	@PrimaryKey
	private String weekRange = "";
	@PrimaryKey
	protected String course = "";
	@PrimaryKey
	private int year = 0;

	public TimetableStub()
	{
		super();
	}

	public String getCourse()
	{
		return course;
	}

	public String getCourseYearCode()
	{
		return String.format(Locale.getDefault(), "%s/%d", course, getYear());
	}

	public boolean isCourseDataSpecified()
	{
		return TextUtils.isEmpty(course) == false && TextUtils.isEmpty(getWeekRange()) == false && getYear() > 0;
	}

	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year = year;
	}

	public String getWeekRange()
	{
		return weekRange;
	}

	public void setWeekRange(String weekRange)
	{
		this.weekRange = weekRange;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof TimetableStub)
		{
			TimetableStub other = (TimetableStub) o;
			return (year == other.year && course.equals(other.course) && weekRange.equals(other.weekRange));
		}
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return course.hashCode() * weekRange.hashCode() * year;
	}

	@Override
	public int compareTo(TimetableStub another)
	{
		int result = course.compareTo(another.course);
		if (result == 0)
		{
			if (year > another.year)
				return 1;
			else if (year < another.year)
				return -1;
			else 
				return compareWeeks(weekRange, another.weekRange);
		}
		return result;
	}
	
	private static int compareWeeks(String weekRange1, String weekRange2)
	{
		Integer 
			firstWeek1 = Integer.valueOf(weekRange1.split(",")[0].split("-")[0]),
			firstWeek2 = Integer.valueOf(weekRange2.split(",")[0].split("-")[0]);
		return firstWeek1.compareTo(firstWeek2);
	}
	
	/**
	 * Change settings to display this timetable as default
	 * @param context
	 * @param settings
	 */
	public void setAsDefault(Context context, AppSettings settings)
	{
		settings.setCourse(course);
		settings.setYear(year);
		settings.setWeekRange(weekRange);
		settings.saveSettings(context);
	}
	
	public CharSequence describe()
	{
		return new StringBuilder(course).append('-').append(getYear());
	}
	
	public CharSequence describeWeeks()
	{
		if (getWeekRange().equals(SEMESTER_1)) return "Semester 1";
		else if (getWeekRange().equals(SEMESTER_2)) return "Semester 2";
		else if (getWeekRange().equals(ALL_WEEKS)) return "Year "+String.valueOf(getYear());
		return new StringBuilder("Weeks ").append(getWeekRange());
	}
}