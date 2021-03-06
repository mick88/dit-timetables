package com.mick88.dittimetable.timetable_activity;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.mick88.dittimetable.timetable.Timetable;

/**
 * This class handles pages in the main screen.
 *
 */
public class TimetablePageAdapter extends FragmentPagerAdapter
{	
	public TimetablePageAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0)
	{
		return DayFragment.newInstance(arg0);
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		
		SpannableString spannableString = new SpannableString(Timetable.DAY_NAMES[position]);
		if (Timetable.getTodayId(false) == position) 
		{
			spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannableString;
	}
	
	@Override
	public int getCount()
	{
		return Timetable.NUM_DAYS;
	}
}
