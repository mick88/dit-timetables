package com.mick88.dittimetable.swipable_tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.mick88.dittimetable.timetable.Timetable;

/**
 * This class handles pages in the main screen.
 *
 */
public class TimetablePageAdapter extends FragmentPagerAdapter
{
	DayFragment[] fragments;
	Timetable timetable;
	
	public TimetablePageAdapter(FragmentManager fm, Timetable timetable)
	{
		super(fm);
		this.fragments = new DayFragment[] {
				new DayFragment(),
				new DayFragment(),
				new DayFragment(),
				new DayFragment(),
				new DayFragment(),
		};
		
		
		int n=0;
		for (DayFragment fragment : fragments)
		{			
			Bundle args = new Bundle();
			args.putInt(DayFragment.EXTRA_DAY_ID, n++);
			fragment.setArguments(args);
		}
		setTimetable(timetable);
	}
	
	public TimetablePageAdapter setTimetable(Timetable timetable)
	{
		this.timetable = timetable;
		int i=0;
		for (DayFragment fragment : fragments)
			fragment.setTimetableDay(timetable.getDay(i++));
		return this;
	}

	@Override
	public Fragment getItem(int arg0)
	{
		/*DayFragment fragment = new DayFragment();
		Bundle args = new Bundle();
		args.putInt("day_id", arg0);
		fragment.setArguments(args);
		return fragment;*/
		return fragments[arg0];
	}
	
	@Override
	public int getItemPosition(Object object)
	{
		return POSITION_NONE;
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		return fragments[position].getDayName();
//		return timetable.getDayTimetable(position).getName();
	}
	
	@Override
	public int getCount()
	{
		return fragments.length;
	}
	
	public void refresh()
	{
		Log.d(toString(), "Refreshing Timetable pages...");
		for (DayFragment fragment : fragments)
			fragment.refresh();
	}
}
