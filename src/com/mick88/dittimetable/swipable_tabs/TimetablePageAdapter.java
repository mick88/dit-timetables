package com.mick88.dittimetable.swipable_tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * This class handles pages in the main screen.
 *
 */
public class TimetablePageAdapter extends FragmentPagerAdapter
{
	DayFragment[] fragments;
	
	public TimetablePageAdapter(FragmentManager fm)
	{
		super(fm);
		this.fragments = new DayFragment[] {
				new DayFragment(),
				new DayFragment(),
				new DayFragment(),
				new DayFragment(),
				new DayFragment(),
		};
		
		for (int i=0; i < fragments.length; i++)
		{
			Bundle args = new Bundle();
			args.putInt(DayFragment.EXTRA_DAY_ID, i);
			fragments[i].setArguments(args);
		}
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
