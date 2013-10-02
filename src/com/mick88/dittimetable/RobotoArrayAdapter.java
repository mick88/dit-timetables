package com.mick88.dittimetable;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mick88.dittimetable.utils.FontApplicator;

public class RobotoArrayAdapter<T> extends ArrayAdapter<T>
{
	FontApplicator fontApplicator;
	
	private void init()
	{
		fontApplicator = new FontApplicator(getContext().getAssets(), TimetableApp.FONT_NAME);
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	public RobotoArrayAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		
		init();
	}
	
	

	public RobotoArrayAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		super(context, resource, textViewResourceId, objects);
		
		init();
	}



	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View result = super.getView(position, convertView, parent);
		
		if (convertView == null) 
			fontApplicator.applyFont(result);
		
		return result;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		View result =  super.getDropDownView(position, convertView, parent);
		
		if (convertView == null) 
			fontApplicator.applyFont(result);
		
		return result;
	}

}
