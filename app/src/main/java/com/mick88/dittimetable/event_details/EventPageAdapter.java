package com.mick88.dittimetable.event_details;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.mick88.dittimetable.timetable.TimetableEvent;

import java.util.List;

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
		Fragment fragment = new EventDetailsFragment();
		Bundle args = new Bundle();
		args.putSerializable(EventDetailsFragment.EXTRA_EVENT, events.get(arg0));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount()
	{
		return events.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		TimetableEvent event = events.get(position);
		
		SpannableString spannableString = new SpannableString(event.getStartTime());
		if (event.isEventOn()) 
		{
			spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannableString;
	}
}
