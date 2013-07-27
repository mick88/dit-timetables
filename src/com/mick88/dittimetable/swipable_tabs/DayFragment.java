package com.mick88.dittimetable.swipable_tabs;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter;
import com.mick88.dittimetable.screens.TimetableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;

public class DayFragment extends Fragment
{
	public static final String EXTRA_DAY_ID = "day_id",
			EXTRA_DAY_NAME = "day_name";
	TimetableDay timetableDay = null;
	EventAdapter eventAdapter = null;
	int dayId;
	private ListView listView;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		dayId =  getArguments().getInt(EXTRA_DAY_ID);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);		
		
		if (activity instanceof TimetableActivity)
		{
			this.timetableDay = ((TimetableActivity) activity).getTimetable().getDay(dayId);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.day_layout, container, false);
	}
	
	public String getDayName()
	{
		return Timetable.DAY_NAMES[dayId];
	}
	
	public void refresh()
	{
		if (timetableDay != null)
		{
			Log.d(toString(), "Refreshing "+getDayName());
			eventAdapter = new EventAdapter(getActivity(), new ArrayList<EventAdapter.EventItem>(timetableDay.getTimetableEntries()));
			listView.setAdapter(eventAdapter);
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(android.R.id.list);
		try
		{
			refresh();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
