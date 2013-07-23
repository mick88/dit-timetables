package com.mick88.dittimetable.screens;

import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.mick88.dittimetable.R;

public class EventDetailsActivity extends SherlockActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = this.getIntent();
		int eventId = intent.getExtras().getInt("event_id");
		
		if (eventId == 0) finish();
		
		displayData(intent.getExtras());
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
	
	private void displayData(Bundle bundle)
	{
		ViewGroup parent = (ViewGroup) findViewById(R.id.eventDetailsContainer);
		
		String name = bundle.getString("name");
		
		parent.addView(newKeyValueView("Module:", name));
		parent.addView(newKeyValueView("Time:", String.format(Locale.getDefault(), "%s - %s", bundle.getString("startTime"), bundle.getString("endTime"))));
		parent.addView(newKeyValueView("Room:", bundle.getString("room")));
		parent.addView(newKeyValueView("Lecturer:", bundle.getString("lecturer")));
		parent.addView(newKeyValueView("Type:", bundle.getString("type")));
		parent.addView(newKeyValueView("Group:", bundle.getString("groups")));
		parent.addView(newKeyValueView("Weeks:", bundle.getString("weeks")));
		this.setTitle(name);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item)
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
