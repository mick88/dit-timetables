package com.mick88.dittimetable;

import java.util.List;

import com.mick88.dittimetable.utils.FontApplicator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class RobotoArrayAdapter<T> extends ArrayAdapter<T>
{
	final FontApplicator fontApplicator;
	
	public RobotoArrayAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		
		fontApplicator = new FontApplicator(context.getAssets(), "Roboto-Light.ttf");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View result = super.getView(position, convertView, parent);
		
		fontApplicator.applyFont(result);
		
		return result;
	}

}
