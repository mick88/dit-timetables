package com.mick88.dittimetable.timetable;

import java.io.Serializable;
import java.util.Locale;

import android.text.TextUtils;

import com.michaldabski.msqlite.Annotations.PrimaryKey;
import com.michaldabski.msqlite.Annotations.TableName;

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
			// TODO: Compare weeks
		}
		return result;
	}
	
}