package com.mick88.dittimetable;

import java.util.List;

import android.content.Context;

import com.michaldabski.msqlite.MSQLiteOpenHelper;
import com.mick88.dittimetable.timetable.Timetable;

public class DatabaseHelper extends MSQLiteOpenHelper
{
	private static final String name = "timetables.db";
	private static final int version = 1;
	
	public DatabaseHelper(Context context) {
		super(context, name, null, version, new Class<?>[]{
				Timetable.class,
		});
	}
	
	public Timetable getTimetable(String course, int year, String weeks)
	{
		List<Timetable> timetables = select(Timetable.class, "course=? AND year=? and weekRange=?", 
				new String[]{course, String.valueOf(year), weeks}, 
				null, "1");
		if (timetables.isEmpty()) return null;
		return timetables.get(0);
	}
	
	public void saveTimetable(Timetable timetable)
	{
		replace(timetable);
	}
}
