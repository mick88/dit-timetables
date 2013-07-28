package com.mick88.dittimetable.list;

import java.io.Serializable;
import java.util.Collection;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.UnfoldActivity;
import com.mick88.dittimetable.list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.utils.FontApplicator;

public class MultiEvent implements EventItem, OnClickListener
{
	private final Collection<TimetableEvent> events;
	private final static int MARGIN_INCREMENT = 25;;
	
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
		final ViewGroup viewGroup;
		Stack<View> recyclableViews = new Stack<View>();
		if (convertView != null)
		{
			viewGroup = (ViewGroup) convertView;
			for (int i=0; i < viewGroup.getChildCount(); i++)
				recyclableViews.push(viewGroup.getChildAt(i));
			viewGroup.removeAllViews();
		}
		else viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.timetable_event_multi, parent, false);

		viewGroup.setOnClickListener(this);

		int margin = dp * events.size();

		for (TimetableEvent event : events)
		{
			margin -= dp;
			View recycle = recyclableViews.isEmpty() ? null : recyclableViews.pop();
			View eventTile = event.getView(layoutInflater, recycle, viewGroup, fontApplicator);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, margin, 0, 0);
			eventTile.setLayoutParams(params);
			viewGroup.addView(eventTile);
		}
		
		return viewGroup;
	}

	@Override
	public void onClick(View v)
	{
		Log.d("Multiview", "Multiview clicked "+v.toString());
		Context context = v.getContext();
		context.startActivity(new Intent(context, UnfoldActivity.class).putExtra(UnfoldActivity.EXTRA_EVENTS, (Serializable)this.events));
	}

}
