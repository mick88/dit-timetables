package com.mick88.dittimetable.swipable_tabs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.screens.TimetableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;

public class DayFragment extends Fragment
{
	TimetableActivity activity=null;

	public DayFragment()
	{
	
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.activity = (TimetableActivity) getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		Context context = new ContextThemeWrapper(activity, R.style.AppTheme);
		inflater = inflater.cloneInContext(context);

		int day_id = getArguments().getInt("day_id");
		
		try
		{
			Timetable timetable = activity.getTimetable();
			TimetableDay timetableDay = timetable.getDayTimetable(day_id);
			return timetableDay.getView(inflater, activity.getApplicationContext());
		}
		catch (Exception e)
		{
			Log.e("DayFragment", e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
}
