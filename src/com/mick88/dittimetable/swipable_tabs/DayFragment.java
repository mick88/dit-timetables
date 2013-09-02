package com.mick88.dittimetable.swipable_tabs;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.screens.TimetableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.utils.FontApplicator;

public class DayFragment extends Fragment
{
	public static final String EXTRA_DAY_ID = "day_id";
	public int dayId;
	
	TimetableDay timetableDay = null;
	Timetable timetable;
	
	TextView tvText;
	private ListView listView;
	FontApplicator fontApplicator = null;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		
		if (getActivity() instanceof TimetableActivity)
		{
			setTimetable(((TimetableActivity)getActivity()).getTimetable());
		}
		
		this.dayId = getArguments().getInt(EXTRA_DAY_ID);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		if (getActivity() instanceof TimetableActivity)
		{
			setTimetable(((TimetableActivity)getActivity()).getTimetable());
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(EXTRA_DAY_ID, dayId);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);		
		fontApplicator = new FontApplicator(activity.getApplicationContext().getAssets(), "Roboto-Light.ttf");
	}
	
	public void setTimetable(Timetable timetable)
	{
		this.timetable = timetable;
		if (timetable != null) setTimetableDay(timetable.getDay(dayId));
	}
	
	DayFragment setTimetableDay(TimetableDay timetableDay)
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
		if (timetableDay != null && listView != null)
		{
			List<EventItem> items = timetableDay.getTimetableEntries(timetable.getSettings());
			if (items.isEmpty())
			{
				listView.setVisibility(View.GONE);
				tvText.setVisibility(View.VISIBLE);
				tvText.setText("No events");				
			}
			else
			{
				listView.setAdapter(new EventAdapter(getActivity(), items, timetableDay));
				listView.setVisibility(View.VISIBLE);
				tvText.setVisibility(View.GONE);
			}
			Log.d(getDayName(), "Refreshed");
		}
		else Log.e("ERROR", "timetable day or listview is null!");
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if (fontApplicator != null) fontApplicator.applyFont(view);
		
		listView = (ListView) view.findViewById(android.R.id.list);
		tvText = (TextView) view.findViewById(R.id.tvDayMessage);
		
		refresh();
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		this.listView = null;
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		timetable = null;
	}
}
