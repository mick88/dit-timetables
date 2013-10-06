package com.mick88.dittimetable.event_details;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.TimetableEvent;

public class EventDetailsFragment extends Fragment
{
	public static final String 
		EXTRA_EVENT = "event";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.activity_event_details, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		TimetableEvent timetableEvent = (TimetableEvent) getArguments().getSerializable(EXTRA_EVENT);
		
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setAdapter(new KeyValueAdapter(view.getContext(), getKeyValuePairs(timetableEvent)));
	}
	
	private List<KeyValue> getKeyValuePairs(TimetableEvent event)
	{
		List<KeyValue> result = new ArrayList<KeyValue>();
		String name = event.getName();
		
		result.add(new KeyValue("Module:", name));
		result.add(new KeyValue("Time:", String.format(Locale.getDefault(), "%s - %s", event.getStartTime(), event.getEndTime())));
		result.add(new KeyValue("Room:", event.getRoomStacked()));
		result.add(new KeyValue("Lecturer:",event.getLecturer()));
		result.add(new KeyValue("Type:", event.getType().toString()));
		
		final String groups;
		Collection<String> groupsCollection = event.getGroups();
		if (groupsCollection.isEmpty())
		{
			groups = "All";
		}
		else
		{		
			StringBuilder groupsBuilder = new StringBuilder();
			String glue = "";
			for (String group : groupsCollection)
			{
				groupsBuilder.append(glue).append(group);
				glue = "\n";
			}
			groups = groupsBuilder.toString();
		}

		result.add(new KeyValue("Groups:", groups));
		result.add(new KeyValue("Weeks:", event.getWeeks()));
		result.add(new KeyValue("Duration:", String.format("%d hours", event.getLength())));
		result.add(new KeyValue("ID:", String.valueOf(event.getId())));
		
		return result;
	}
}
