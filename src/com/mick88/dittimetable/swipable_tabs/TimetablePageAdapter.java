package com.mick88.dittimetable.swipable_tabs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mick88.dittimetable.timetable.Timetable;

/**
 * This class handles swiping pages in the main screen.
 *
 */
public class TimetablePageAdapter extends FragmentPagerAdapter
{
	Timetable timetable;
	Context context;
	private TimetablePageAdapter(FragmentManager fm)
	{
		super(fm);
	}
	
	public TimetablePageAdapter(FragmentManager fragmentManager, Timetable timetable, Context context)
	{
		this(fragmentManager);
		this.timetable = timetable;
	}

	@Override
	public Fragment getItem(int arg0)
	{
		DayFragment fragment = new DayFragment();
		Bundle args = new Bundle();
		args.putInt("day_id", arg0);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		return timetable.getDayTimetable(position).getName();
	}
	
	@Override
	public int getCount()
	{
		return 5;
	}
	
	@Override
	public int getItemPosition(Object object)
	{
		return POSITION_NONE; // allows refreshing
//		return super.getItemPosition(object);
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}
	
}
