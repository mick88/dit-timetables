package com.mick88.dittimetable.swipable_tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mick88.dittimetable.timetable.Timetable;

/**
 * This class handles pages in the main screen.
 *
 */
public class TimetablePageAdapter extends FragmentPagerAdapter
{
	Timetable timetable;
	
	public TimetablePageAdapter(FragmentManager fm, Timetable timetable)
	{
		super(fm);
		setTimetable(timetable);
	}
	
	public TimetablePageAdapter setTimetable(Timetable timetable)
	{
		this.timetable = timetable;
		notifyDataSetChanged();
		return this;
	}

	@Override
	public Fragment getItem(int arg0)
	{
		DayFragment fragment = new DayFragment();
		Bundle args = new Bundle();
		args.putSerializable(DayFragment.EXTRA_DAY, timetable.getDay(arg0));
		args.putSerializable(DayFragment.EXTRA_SETTINGS, timetable.getSettings());
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public int getItemPosition(Object object)
	{
		return POSITION_NONE;
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		return Timetable.DAY_NAMES[position];
	}
	
	@Override
	public int getCount()
	{
		return 5;
	}
}
