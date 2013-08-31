package com.mick88.dittimetable.event_details;

import java.util.List;

import com.mick88.dittimetable.utils.FontApplicator;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class KeyValueAdapter extends ArrayAdapter<KeyValue>
{
	FontApplicator 
		fontApplicator;
	
	public KeyValueAdapter(Context context, List<KeyValue> objects) {
		super(context, 0, 0, objects);
		this.fontApplicator = new FontApplicator(context.getAssets(), "Roboto-Light.ttf");
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(android.R.layout.two_line_list_item, parent, false);
		
		TextView tvName, tvValue;
		
		tvName = (TextView) view.findViewById(android.R.id.text1);
		tvValue = (TextView) view.findViewById(android.R.id.text2);
		
		fontApplicator.applyFont(view);
		
		tvName.setText(getItem(position).key);
		tvValue.setText(getItem(position).value);
				
		return view;
	}
	
}
