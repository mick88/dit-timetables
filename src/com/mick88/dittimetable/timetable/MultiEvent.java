package com.mick88.dittimetable.timetable;

import java.util.Collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.swipable_tabs.EventAdapter.EventItem;

public class MultiEvent implements EventItem
{
	private final Collection<TimetableEvent> events;	
	
	public MultiEvent(Collection<TimetableEvent> events)
	{
		this.events = events;
	}
	
	@Override
	public int getViewType()
	{
		return ITEM_TYPE_MULTIEVENT;
	}

	@Override
	public View getView(LayoutInflater layoutInflater, ViewGroup parent)
	{
		ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.timetable_event_multi, parent);
		for (TimetableEvent event : events)
		{
			// TODO: add cascading event tiles
			/*View eventTile = layoutInflater.inflate(R.layout.timetable_event_small, viewGroup, false);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.
			eventTile.setLayoutParams(new LayoutParams(layoutInflater.getContext(), ))*/
		}
		return viewGroup;
	}

}
