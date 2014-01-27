package com.mick88.dittimetable.timetable_activity;

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
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.utils.FontApplicator;

public class DayFragment extends Fragment
{
	public static final String
		EXTRA_DAY_ID = "day_id";
	
	int dayId;
	TimetableActivity activity;
	AppSettings appSettings = null;
	
	TextView tvText;
	private ListView listView;
	FontApplicator fontApplicator = null;
	
	private Timetable getTimetable()
	{
		return activity.getTimetable();
	}
	
	private TimetableDay getTimetableDay()
	{
		return getTimetable().getDay(dayId);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.dayId = getArguments().getInt(EXTRA_DAY_ID);
		TimetableApp application = (TimetableApp) getActivity().getApplication();
		appSettings = application.getSettings();
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		fontApplicator = new FontApplicator(activity.getApplicationContext().getAssets(), TimetableApp.FONT_NAME);
		this.activity = (TimetableActivity) activity;
		this.activity.addFragment(this);
	}
	
	@Override
	public void onDetach()
	{
		this.activity.removeFragment(this);
		this.activity = null;
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.day_layout, container, false);
	}
	
	public String getDayName()
	{
		return getTimetableDay().getName();
	}
	
	public void refresh()
	{
		if (listView != null)
		{
			List<EventItem> items = getTimetableDay().getTimetableEntries(appSettings);
			if (items.isEmpty())
			{
				listView.setVisibility(View.GONE);
				tvText.setVisibility(View.VISIBLE);
				tvText.setText(R.string.no_events);				
			}
			else
			{
				listView.setAdapter(new EventAdapter(getActivity(), items, getTimetableDay(), getTimetable()));
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
		
		View header = getLayoutInflater(savedInstanceState)
				.inflate(R.layout.timetable_list_header, listView, false);
		listView.addHeaderView(header);
		
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
