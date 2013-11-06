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
		EXTRA_DAY = "day",
		EXTRA_TIMETABLE = "timetable";
	
	TimetableDay timetableDay = null;
	Timetable timetable = null;
	AppSettings appSettings = null;
	
	TextView tvText;
	private ListView listView;
	FontApplicator fontApplicator = null;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		TimetableApp application = (TimetableApp) getActivity().getApplication();
		appSettings = application.getSettings();
		timetableDay = (TimetableDay) getArguments().getSerializable(EXTRA_DAY);
		timetable = (Timetable) getArguments().getSerializable(EXTRA_TIMETABLE);
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);		
		fontApplicator = new FontApplicator(activity.getApplicationContext().getAssets(), "Roboto-Light.ttf");
		if (activity instanceof TimetableActivity)
		{
			((TimetableActivity) activity).addFragment(this);
		}
	}
	
	@Override
	public void onDetach()
	{
		if (getActivity() instanceof TimetableActivity)
		{
			((TimetableActivity) getActivity()).removeFragment(this);
		}
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
				listView.setAdapter(new EventAdapter(getActivity(), items, timetableDay, timetable));
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
