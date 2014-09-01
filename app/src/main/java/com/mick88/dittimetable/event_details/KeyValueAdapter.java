package com.mick88.dittimetable.event_details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.utils.FontApplicator;

import java.util.List;

public class KeyValueAdapter extends ArrayAdapter<KeyValue>
{
	FontApplicator 
		fontApplicator;
	
	private static class ViewHolder
	{
		TextView tvName, tvValue;
	}
	
	public KeyValueAdapter(Context context, List<KeyValue> objects) {
		super(context, 0, 0, objects);
		this.fontApplicator = new FontApplicator(context.getAssets(), TimetableApp.FONT_NAME);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final View view;
		final ViewHolder holder;
		
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_item_detail, parent, false);
			fontApplicator.applyFont(view);
			
			holder = new ViewHolder();
			holder.tvName = (TextView) view.findViewById(android.R.id.text1);
			holder.tvValue = (TextView) view.findViewById(android.R.id.text2);
			
			view.setTag(holder);
		}
		else
		{
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		holder.tvName.setText(getItem(position).key);
		holder.tvValue.setText(getItem(position).value);
				
		return view;
	}
	
}
