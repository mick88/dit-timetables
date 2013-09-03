package com.mick88.dittimetable.event_details;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mick88.dittimetable.timetable.TimetableEvent;

public class EventPageAdapter extends FragmentPagerAdapter
{
	final List<TimetableEvent> events;
	
	public EventPageAdapter(FragmentManager fm, List<TimetableEvent> events) 
	{
		super(fm);
		this.events = events;
	}

	@Override
	public Fragment getItem(int arg0)
	{
		return null;
	}

	@Override
	public int getCount()
	{
		return events.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		return events.get(position).getName();
	}
}
