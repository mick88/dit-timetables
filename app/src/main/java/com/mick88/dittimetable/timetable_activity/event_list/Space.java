package com.mick88.dittimetable.timetable_activity.event_list;

import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.utils.FontApplicator;

/**
 * Represents space between events
 *
 */
public class Space implements EventItem
{
	private final int 
		numSpaces,
		startHour;
	
	public Space(int numSpaces, int startHour) 
	{
		this.numSpaces = numSpaces;
		this.startHour = startHour;
	}

	@Override
	public int getViewType()
	{
		return EventItem.ITEM_TYPE_SEPARATOR;
	}

	@Override
	public View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent, FontApplicator fontApplicator, boolean allowHighlight, Timetable timetable)
	{
		final int current = getCurrentSpace();
		
		ViewGroup space = (ViewGroup) layoutInflater.inflate(R.layout.timetable_event_empty, parent, false);
		ViewGroup container = (ViewGroup) space.findViewById(R.id.separator_dot_container);
		
		Context context = layoutInflater.getContext();
		int separator = (int) context.getResources().getDimension(R.dimen.dot_spacing);
		
		for (int i=0; i < numSpaces; i++)
		{
			ImageView imageView = new ImageView(context);
			
			if (allowHighlight && (i == current))
				imageView.setImageResource(R.drawable.dot_selected);
			else
				imageView.setImageResource(R.drawable.dot);
			
			imageView.setPadding(separator, separator, separator, separator);
			
			container.addView(imageView);
		}
		return space;
	}
	
	private int getCurrentSpace()
	{
		return (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) - startHour;
	}

}
