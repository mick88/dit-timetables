package com.mick88.dittimetable;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.michaldabski.msqlite.MSQLiteOpenHelper;
import com.mick88.dittimetable.settings.AppSettings;
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
		try
		{
			List<Timetable> timetables = select(Timetable.class, "course=? AND year=? and weekRange=?", 
					new String[]{course, String.valueOf(year), weeks}, 
					null, "1");
			if (timetables.isEmpty()) return null;
			Log.d(TAG, "Timetable loaded: "+timetables.get(0).describe());
			return timetables.get(0);
		}
		catch (SQLiteException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Timetable loadTimetable(AppSettings appSettings)
	{
		return getTimetable(appSettings.getCourse(), appSettings.getYear(), appSettings.getWeekRange());
	}
	
	public Timetable loadTimetable(TimetableStub timetableStub)
	{
		return getTimetable(timetableStub.getCourse(), timetableStub.getYear(), timetableStub.getWeekRange());
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
		SQLiteDatabase database = getReadableDatabase();
		List<TimetableStub> timetableStubs = select(database, TimetableStub.class, new String [] {"course", "year", "weekRange"}, null, null, "course, year", null);
		Log.d("Timetables loaded", timetableStubs.toString());
		database.close();
		return timetableStubs;
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		onUpgrade(db, oldVersion, newVersion);
	}
	
	public void deleteAllTimetables()
	{
		SQLiteDatabase database = getWritableDatabase();
		deleteFrom(database, Timetable.class, null, null);
		database.close();
	}
}
