package com.mick88.dittimetable.event_details;

import java.util.List;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;

public class EventPageAdapter extends FragmentPagerAdapter
{
	final List<TimetableEvent> events;
	final TimetableDay timetableDay;
	
	public EventPageAdapter(FragmentManager fm, List<TimetableEvent> events, TimetableDay timetableDay) 
	{
		super(fm);
		this.events = events;
		this.timetableDay = timetableDay;
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
		if (timetableDay.isToday() && event.isEventOn()) 
		{
			spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannableString;
	}
}
