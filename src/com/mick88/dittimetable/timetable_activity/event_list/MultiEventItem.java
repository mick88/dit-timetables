package com.mick88.dittimetable.timetable_activity.event_list;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.UnfoldActivity;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable_activity.event_list.SingleEventItem.EventViewHolder;
import com.mick88.dittimetable.utils.FontApplicator;

public class MultiEventItem implements EventItem, OnClickListener
{
	private final List<SingleEventItem> events;
	private final Timetable timetable;
	WeakReference<ViewGroup> containeReference = null;
	
	public MultiEventItem(List<SingleEventItem> events, Timetable timetable)
	{
		this.events = events;
		this.timetable = timetable;
	}
	
	@Override
	public int getViewType()
	{
		return ITEM_TYPE_MULTIEVENT;
	}

	@Override
	public View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent, FontApplicator fontApplicator, boolean allowHighlight, final Timetable timetable)
	{
		Resources resources = layoutInflater.getContext().getResources();
		int offset = (int)(resources.getDimension(R.dimen.multievent_offset));
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
		this.containeReference = new WeakReference<ViewGroup>(viewGroup);

		int margin = (int)(offset * events.size());

		for (int i=events.size()-1; i >= 0; i--)
		{
			margin -= offset;
			if (margin < 0) 
			{
				Log.e("Margin is negative!", String.valueOf(margin));
				margin = 0;
			}
			View recycle = recyclableViews.isEmpty() ? null : recyclableViews.pop();
			View eventTile = events.get(i).getView(layoutInflater, recycle, viewGroup, fontApplicator, allowHighlight, timetable);
			EventViewHolder eventViewHolder = (EventViewHolder) eventTile.getTag();
			eventViewHolder.eventTile.setOnClickListener(this);
			eventTile.setClickable(false);
			LayoutParams params = (LayoutParams) eventTile.getLayoutParams();
			params.setMargins(0, margin, 0, 0);
			eventTile.setLayoutParams(params);
			viewGroup.addView(eventTile);
		}
		
		return viewGroup;
	}

	@Override
	public void onClick(View v)
	{
		ViewGroup container = this.containeReference.get();
		if (container == null) return;
		
		if (v instanceof ViewGroup)
		{
			Context context = v.getContext();
			int space = (int) context.getResources().getDimension(R.dimen.multievent_offset);
			
			List<TimetableEvent> events = new ArrayList<TimetableEvent>(MultiEventItem.this.events.size());
			List<Integer> positions = new ArrayList<Integer>(events.size());
			for (SingleEventItem event : MultiEventItem.this.events)
			{
				events.add(event.getEvent());
			}

			for (int i=container.getChildCount()-1; i  >= 0 ; i--)
			{
				View child = container.getChildAt(i);
				int[] location = new int[2];
				child.getLocationOnScreen(location);
				positions.add(location[1] - space);
			}
			context.startActivity(new Intent(context, UnfoldActivity.class)
				.putExtra(UnfoldActivity.EXTRA_EVENTS, (Serializable)events)
				.putExtra(UnfoldActivity.EXTRA_POSITIONS, (Serializable)positions)
				.putExtra(UnfoldActivity.EXTRA_TIMETABLE, timetable));
		}		
	}
}
