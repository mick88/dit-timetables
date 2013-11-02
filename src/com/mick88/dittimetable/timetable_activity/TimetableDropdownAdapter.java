package com.mick88.dittimetable.timetable_activity;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.RobotoArrayAdapter;
import com.mick88.dittimetable.timetable.Timetable;

public class TimetableDropdownAdapter extends RobotoArrayAdapter<Timetable>
{
	private static class ViewHolder
	{
		TextView tvCourse, tvWeeks;
		public ViewHolder(View view)
		{
			tvCourse = (TextView) view.findViewById(android.R.id.text1);
			tvWeeks = (TextView) view.findViewById(android.R.id.text2);
		}
	}
	public TimetableDropdownAdapter(Context context, List<Timetable> objects)
	{
		super(context, 0, View.NO_ID, objects);
	}
	
	private LayoutInflater getLayoutInflater()
	{
		return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		if (view == null)
		{
			view = getLayoutInflater().inflate(R.layout.list_item_dropdown_2line, parent, false);
			view.setTag(new ViewHolder(view));
			applyRobotoFont(view);
		}
		setData(view, getItem(position));
		return view;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		if (view == null)
		{
			view = getLayoutInflater().inflate(R.layout.list_item_dropdown_2line, parent, false);
			view.setTag(new ViewHolder(view));
			applyRobotoFont(view);
		}
		setData(view, getItem(position));
		return view;
	}
	
	void setData(View view, Timetable timetable)
	{
		ViewHolder holder = (ViewHolder) view.getTag();
		
		holder.tvCourse.setText(timetable.describe());
		holder.tvWeeks.setText(timetable.describeWeeks());
	}
}
