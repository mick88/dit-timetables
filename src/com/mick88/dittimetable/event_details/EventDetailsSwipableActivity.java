package com.mick88.dittimetable.event_details;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.event_list.EventAdapter.EventItem;
import com.mick88.dittimetable.utils.FontApplicator;

public class EventDetailsSwipableActivity extends ActionBarActivity
{
	public static final String
		EXTRA_SELECTED_EVENT = "event",
		EXTRA_DAY = "day";
	
	private TimetableDay timetableDay;
	private AppSettings appSettings;
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);
		
		new FontApplicator(getAssets(), TimetableApp.FONT_NAME).applyFont(getWindow().getDecorView());
		
		Bundle extras = getIntent().getExtras();
		TimetableEvent selectedEvent;
		TimetableApp application = (TimetableApp) getApplication();
		this.appSettings = application.getSettings();
		
		if (extras != null)
		{
			this.timetableDay = (TimetableDay) extras.getSerializable(EXTRA_DAY);
			selectedEvent = (TimetableEvent) extras.getSerializable(EXTRA_SELECTED_EVENT);
		}
		else
		{
			finish();
			return;
		}
		
		setTitle(timetableDay.getName());
		getSupportActionBar().setSubtitle(timetableDay.getHoursRange(appSettings));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		List<TimetableEvent> events = new ArrayList<TimetableEvent>();
		for (EventItem item : timetableDay.getEvents(appSettings))
		{
			if (item instanceof TimetableEvent)
				events.add((TimetableEvent) item);
		}
//		events.addAll((Collection<? extends TimetableEvent>) timetableDay.getTimetableEntries(appSettings));
		viewPager.setAdapter(new EventPageAdapter(getSupportFragmentManager(), events));
		int page = events.indexOf(selectedEvent);
		viewPager.setCurrentItem(page, false);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
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
