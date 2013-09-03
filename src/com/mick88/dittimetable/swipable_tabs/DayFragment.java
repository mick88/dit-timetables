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

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.screens.TimetableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.utils.FontApplicator;

public class DayFragment extends Fragment
{
	public static final String
		EXTRA_DAY = "day",
		EXTRA_SETTINGS ="settings";
	
	TimetableDay timetableDay = null;
	AppSettings appSettings = null;
	
	TextView tvText;
	private ListView listView;
	FontApplicator fontApplicator = null;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		
		timetableDay = (TimetableDay) getArguments().getSerializable(EXTRA_DAY);
		appSettings = (AppSettings) getArguments().getSerializable(EXTRA_SETTINGS);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);		
		fontApplicator = new FontApplicator(activity.getApplicationContext().getAssets(), "Roboto-Light.ttf");
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
		if (listView != null)
		{
			List<EventItem> items = timetableDay.getTimetableEntries(appSettings);
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
		this.tvText = null;
	}
}
