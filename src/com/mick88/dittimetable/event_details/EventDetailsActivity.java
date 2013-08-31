package com.mick88.dittimetable.event_details;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.TimetableEvent;

public class EventDetailsActivity extends ActionBarActivity
{

	public static final String EXTRA_EVENT = "event";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = this.getIntent();
		Serializable serializable = intent.getSerializableExtra(EXTRA_EVENT);
		
		if (serializable instanceof TimetableEvent)
		{
			TimetableEvent event = (TimetableEvent) serializable;
			List<KeyValue> pairs = getKeyValuePairs(event);
			ListView listView = (ListView) findViewById(android.R.id.list);
			listView.setAdapter(new KeyValueAdapter(this, pairs));
			
			setTitle(event.getName());
		}
		else  
			finish();		
	}
	
	private List<KeyValue> getKeyValuePairs(TimetableEvent event)
	{
		List<KeyValue> result = new ArrayList<KeyValue>();
		String name = event.getName();
		
		result.add(new KeyValue("Module:", name));
		result.add(new KeyValue("Time:", String.format(Locale.getDefault(), "%s - %s", event.getStartTime(), event.getEndTime())));
		result.add(new KeyValue("Room:", event.getRoom()));
		result.add(new KeyValue("Lecturer:",event.getLecturer()));
		result.add(new KeyValue("Type:", event.getType().toString()));
		result.add(new KeyValue("Group:", event.getGroupStr()));
		result.add(new KeyValue("Weeks:", event.getWeeks()));
		
		return result;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(
			MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
