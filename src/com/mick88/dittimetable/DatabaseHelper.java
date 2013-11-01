package com.mick88.dittimetable;

import android.content.Context;

import com.michaldabski.msqlite.MSQLiteOpenHelper;
import com.mick88.dittimetable.timetable.Timetable;

public class DatabaseHelper extends MSQLiteOpenHelper
{
	private static final String name = "timetables.db";
	private static final int version = 1;
	
	public DatabaseHelper(Context context, Class<?>[] trackedClasses) {
		super(context, name, null, version, new Class<?>[]{
				Timetable.class,
		});
	}
	
}
