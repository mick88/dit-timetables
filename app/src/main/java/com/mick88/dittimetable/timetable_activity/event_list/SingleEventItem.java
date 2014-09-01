package com.mick88.dittimetable.timetable_activity.event_list;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.event_details.EventDetailsSwipableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.utils.FontApplicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SingleEventItem implements EventItem, OnClickListener
{
	private final TimetableEvent event;
	private final Timetable timetable;
	
	public static class EventViewHolder
	{
		protected final TextView tvEventTime, tvEventLocation, tvEventTitle, tvEventType, tvEventLecturer, tvEventGroup;
		public final View background, eventTile;
		
		public EventViewHolder(TextView tvEventTime, TextView tvEventLocation,
				TextView tvEventTitle, TextView tvEventType,
				TextView tvEventLecturer, TextView tvEventGroup, View background, View eventTile) {
			this.tvEventTime = tvEventTime;
			this.tvEventLocation = tvEventLocation;
			this.tvEventTitle = tvEventTitle;
			this.tvEventType = tvEventType;
			this.tvEventLecturer = tvEventLecturer;
			this.tvEventGroup = tvEventGroup;
			this.background = background;
			this.eventTile = eventTile;
		}
		
		public EventViewHolder(View view)
		{
			this((TextView)view.findViewById(R.id.eventTime),
					(TextView)view.findViewById(R.id.eventLocation),
					(TextView)view.findViewById(R.id.eventTitle),
					(TextView)view.findViewById(R.id.eventType),
					(TextView)view.findViewById(R.id.eventLecturer),
					(TextView)view.findViewById(R.id.eventGroup),
					view.findViewById(R.id.timetable_event_small),
					view.findViewById(R.id.timetable_event_small));
		}
	}

	private SingleEventItem(TimetableEvent event, Timetable timetable)
	{
		this.event = event;
		this.timetable = timetable;
	}
	
	public static SingleEventItem instantiateForEvent(TimetableEvent event, Timetable timetable)
	{
		return new SingleEventItem(event, timetable);
	}
	
	public static List<SingleEventItem> instantiateForEvents(Collection<TimetableEvent> events, Timetable timetable)
	{
		List<SingleEventItem> result = new ArrayList<SingleEventItem>(events.size());
		for (TimetableEvent event : events) result.add(instantiateForEvent(event, timetable));
		return result;
	}

	@Override
	public int getViewType()
	{
		return ITEM_TYPE_EVENT;
	}

	@Override
	public View getView(LayoutInflater layoutInflater, View convertView, ViewGroup parent, FontApplicator fontApplicator, boolean allowHighlight, final Timetable timetable)
	{
		View view = convertView;
		EventViewHolder viewHolder;
		if (view == null)
		{
			view = layoutInflater.inflate(R.layout.timetable_event, parent, false);
			if (fontApplicator != null) fontApplicator.applyFont(view);
			viewHolder = new EventViewHolder(view);
			view.setTag(viewHolder);
		}
		else viewHolder = (EventViewHolder) view.getTag();
		
		if (allowHighlight && event.isEventOn())
			viewHolder.background.setBackgroundResource(R.drawable.event_selected_selector);
		else
			viewHolder.background.setBackgroundResource(R.drawable.event_selector);
		
		viewHolder.eventTile.setOnClickListener(this);
		
		viewHolder.tvEventGroup.setText(event.getGroupsString(timetable.getCourse()));
		viewHolder.tvEventLecturer.setText(event.getLecturer());
		viewHolder.tvEventLocation.setText(event.getRoom());
		viewHolder.tvEventTime.setText(event.getEventTimeString());
		viewHolder.tvEventType.setText(event.getEventType().toString());
		viewHolder.tvEventTitle.setText(event.getName());
		
		int colourRes = 0;
		switch (event.getEventType())
		{
			case Laboratory:
				colourRes = R.color.color_laboratory;
				break;
			case Lecture:
				colourRes = R.color.color_lecture;
				break;
			case Tutorial:
				colourRes = R.color.color_tutorial;
				break;			
		}
		
		if (colourRes != 0)
		{
			viewHolder
			.tvEventType
			.setTextColor(viewHolder
                    .tvEventType
                    .getContext()
                    .getResources()
                    .getColor(colourRes));
		}
		return view;		
	}
	
	public TimetableEvent getEvent()
	{
		return event;
	}

	@Override
	public void onClick(View v)
	{
		Context context = v.getContext().getApplicationContext();
		Intent intent = new Intent(context, EventDetailsSwipableActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(EventDetailsSwipableActivity.EXTRA_SELECTED_EVENT, event);
		intent.putExtra(EventDetailsSwipableActivity.EXTRA_DAY, timetable.getDay(event.getDay()));
		context.startActivity(intent);		
	}
	
}
