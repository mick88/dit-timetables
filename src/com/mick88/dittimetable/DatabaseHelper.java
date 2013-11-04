package com.mick88.dittimetable;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.michaldabski.msqlite.MSQLiteOpenHelper;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableStub;

public class DatabaseHelper extends MSQLiteOpenHelper
{
	private static final String name = "timetables.db";
	private static final int version = 1;
	private static final String TAG = "SQLite";
	
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
		Log.d(TAG, "Timetable loaded: "+timetables.get(0).describe());
		return timetables.get(0);
	}
	
	public void saveTimetable(Timetable timetable)
	{
		replace(timetable);
		Log.d(TAG, "Timetable saved: "+timetable.describe());
	}
	
	/**
	 * returns all timetables saved in database
	 * @return
	 */
	public List<TimetableStub> getSavedTimetables()
	{
		return selectAll(TimetableStub.class);
	}
}
