package com.mick88.dittimetable.swipable_tabs;

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
	final Timetable timetable;
	
	public TimetablePageAdapter(FragmentManager fm, Timetable timetable)
	{
		super(fm);
		this.timetable = timetable;
		
		this.fragments = new DayFragment[] {
				new DayFragment().setTimetableDay(timetable.getDay(0)),
				new DayFragment().setTimetableDay(timetable.getDay(1)),
				new DayFragment().setTimetableDay(timetable.getDay(2)),
				new DayFragment().setTimetableDay(timetable.getDay(3)),
				new DayFragment().setTimetableDay(timetable.getDay(4)),
		};
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
	
	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
		refresh();
	}
	
	public void refresh()
	{
		Log.d(toString(), "Refreshing Timetable pages...");
		for (DayFragment fragment : fragments)
			fragment.refresh();
	}
}
