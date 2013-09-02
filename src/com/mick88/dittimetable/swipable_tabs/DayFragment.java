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
import android.widget.TextView;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.utils.FontApplicator;

public class DayFragment extends Fragment
{
	public static final String EXTRA_DAY_ID = "day_id",
			EXTRA_DAY_NAME = "day_name",
			EXTRA_SETTINGS = "app_settings",
			EXTRA_DAY_OBJECT = "day_object";
	TimetableDay timetableDay = null;
	EventAdapter eventAdapter = null;
	TextView tvText;
	int dayId;
	private ListView listView;
	private List<EventItem> events = new ArrayList<EventAdapter.EventItem>();
	FontApplicator fontApplicator = null;
	AppSettings appSettings;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		this.appSettings = (AppSettings) getArguments().getSerializable(EXTRA_SETTINGS);
		
		if (savedInstanceState != null)
		{
			timetableDay = (TimetableDay) savedInstanceState.getSerializable(EXTRA_DAY_OBJECT);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRA_DAY_OBJECT, timetableDay);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);		
		fontApplicator = new FontApplicator(activity.getAssets(), "Roboto-Light.ttf");
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
			List<EventItem> items = timetableDay.getTimetableEntries(appSettings);
			if (items.isEmpty())
			{
				listView.setVisibility(View.GONE);
				tvText.setVisibility(View.VISIBLE);
				tvText.setText("No events");				
			}
			else
			{
				events.addAll(items);
				eventAdapter.notifyDataSetChanged();
				listView.setVisibility(View.VISIBLE);
				tvText.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		if (fontApplicator != null) fontApplicator.applyFont(view);
		
		listView = (ListView) view.findViewById(android.R.id.list);
		eventAdapter = new EventAdapter(getActivity(), events, timetableDay);
		listView.setAdapter(eventAdapter);
		tvText = (TextView) view.findViewById(R.id.tvDayMessage);
		
		refresh();
	}
	
	
}
