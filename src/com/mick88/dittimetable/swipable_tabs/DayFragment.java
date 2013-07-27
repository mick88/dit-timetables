package com.mick88.dittimetable.swipable_tabs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.screens.TimetableActivity;
import com.mick88.dittimetable.timetable.TimetableDay;

public class DayFragment extends Fragment
{
	TimetableDay timetableDay = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		int dayId =  getArguments().getInt("day_id");
		
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
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		
		if (timetableDay != null)
		{
			ListView listView = (ListView) view.findViewById(android.R.id.list);
			List<EventItem> items = new ArrayList<EventAdapter.EventItem>();
			try
			{
				items.addAll(timetableDay.getTimetableEntries());
				listView.setAdapter(new EventAdapter(getActivity(), items));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	
}
