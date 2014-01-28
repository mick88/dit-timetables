package com.mick88.dittimetable.timetable_activity.event_list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.UnfoldActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.timetable_activity.event_list.SingleEventItem.EventViewHolder;
import com.mick88.dittimetable.utils.FontApplicator;

public class MultiEventItem implements EventItem
{
	private final List<SingleEventItem> events;
	@Deprecated
	private final static int MARGIN_INCREMENT = 25;
	
	public MultiEventItem(List<SingleEventItem> events)
	{
		this.events = events;
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
		float offset = (float)(resources.getDimension(R.dimen.multievent_offset));
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
		
		OnClickListener clickListener = new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (v instanceof ViewGroup)
				{
					Context context = v.getContext();
					ArrayList<Integer> positions = new ArrayList<Integer>(events.size());
					ViewGroup viewGroup = (ViewGroup) v;
					for (int i=0; i  < viewGroup.getChildCount(); i++)
					{
						Rect rect = new Rect();
						viewGroup.getChildAt(i).getGlobalVisibleRect(rect);
						positions.add(rect.top);
					}
					List<TimetableEvent> events = new ArrayList<TimetableEvent>(MultiEventItem.this.events.size());
					for (SingleEventItem event : MultiEventItem.this.events)
						events.add(event.getEvent());
					context.startActivity(new Intent(context, UnfoldActivity.class)
						.putExtra(UnfoldActivity.EXTRA_EVENTS, (Serializable)events)
						.putExtra(UnfoldActivity.EXTRA_POSITIONS, positions)
						.putExtra(UnfoldActivity.EXTRA_TIMETABLE, timetable));
				}
			}
		};

		viewGroup.setOnClickListener(clickListener);

		int margin = (int)(offset * events.size());

//		for (TimetableEvent event : events)
		for (int i=events.size()-1; i >= 0; i--)
		{
			margin -= offset;
			View recycle = recyclableViews.isEmpty() ? null : recyclableViews.pop();
			View eventTile = events.get(i).getView(layoutInflater, recycle, viewGroup, fontApplicator, allowHighlight, timetable);
			EventViewHolder eventViewHolder = (EventViewHolder) eventTile.getTag();
			eventViewHolder.eventTile.setOnClickListener(clickListener);
			eventTile.setClickable(false);
			LayoutParams params = (LayoutParams) eventTile.getLayoutParams();
			params.setMargins(0, margin, 0, 0);
			eventTile.setLayoutParams(params);
			viewGroup.addView(eventTile);
		}
		
		return viewGroup;
	}
}
