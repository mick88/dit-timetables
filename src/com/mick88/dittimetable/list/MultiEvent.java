package com.mick88.dittimetable.list;

import java.util.Collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.utils.FontApplicator;

public class MultiEvent implements EventItem
{
	private final Collection<TimetableEvent> events;
	private final static int MARGIN_INCREMENT = 25;
	
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
	public View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent, FontApplicator fontApplicator)
	{
		int dp = (int)(layoutInflater.getContext().getResources().getDisplayMetrics().density * MARGIN_INCREMENT);
		
		ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.timetable_event_multi, parent, false);
		int margin = dp * events.size();
		for (TimetableEvent event : events)
		{
			margin -= dp;
			View eventTile = event.getView(layoutInflater, null, viewGroup, fontApplicator);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, margin, 0, 0);
			eventTile.setLayoutParams(params);
			viewGroup.addView(eventTile);			
		}
		
		return viewGroup;
	}

}
