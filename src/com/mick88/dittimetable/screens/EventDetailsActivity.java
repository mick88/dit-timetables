package com.mick88.dittimetable.screens;

import java.io.Serializable;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

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
			displayData((TimetableEvent) serializable);
		else  
			finish();		
	}
	
	@SuppressWarnings("deprecation")
	private View newKeyValueView(CharSequence caption, CharSequence value)
	{
		LinearLayout result = new LinearLayout(getApplicationContext());
		result.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		result.setOrientation(LinearLayout.VERTICAL);
		
		TextView tvCaption = new TextView(getApplicationContext());
		tvCaption.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tvCaption.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large_Inverse);
		tvCaption.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		tvCaption.setText(caption);
		
		TextView tvValue = new TextView(getApplicationContext());
		tvValue.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tvValue.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium_Inverse);
		tvValue.setGravity(Gravity.RIGHT | Gravity.TOP);
		tvValue.setText(value);
		
		result.addView(tvCaption);
		result.addView(tvValue);
		return result;
	}
	
	private void displayData(TimetableEvent event)
	{
		ViewGroup parent = (ViewGroup) findViewById(R.id.eventDetailsContainer);
		
		String name = event.getName();
		
		parent.addView(newKeyValueView("Module:", name));
		parent.addView(newKeyValueView("Time:", String.format(Locale.getDefault(), "%s - %s", event.getStartTime(), event.getEndTime())));
		parent.addView(newKeyValueView("Room:", event.getRoom()));
		parent.addView(newKeyValueView("Lecturer:",event.getLecturer()));
		parent.addView(newKeyValueView("Type:", event.getType().toString()));
		parent.addView(newKeyValueView("Group:", event.getGroupStr()));
		parent.addView(newKeyValueView("Weeks:", event.getWeeks()));
		this.setTitle(name);
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
