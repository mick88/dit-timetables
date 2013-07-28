package com.mick88.dittimetable.swipable_tabs;

import java.util.ArrayList;
import java.util.List;

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
import com.mick88.dittimetable.list.EventAdapter.EventItem;
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
	private List<EventItem> events = new ArrayList<EventAdapter.EventItem>();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);		
	}
	
	public DayFragment setTimetableDay(TimetableDay timetableDay)
	{
		this.timetableDay = timetableDay;
		refresh();
		return this;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.day_layout, container, false);
	}
	
	public String getDayName()
	{
		return timetableDay.getName();
	}
	
	public void refresh()
	{
		if (timetableDay != null && eventAdapter != null)
		{
			events.clear();
			events.addAll(timetableDay.getTimetableEntries());
			eventAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(android.R.id.list);
		eventAdapter = new EventAdapter(getActivity(), events);
		listView.setAdapter(eventAdapter);
		
		refresh();
	}
	
	
}
