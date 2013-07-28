package com.mick88.dittimetable;

import java.util.Collection;

import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.utils.FontApplicator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class UnfoldActivity extends Activity
{
	/**
	 * Extra argument containing a Collection of TimetableEvents
	 * as a Serializable object
	 */
	public static final String EXTRA_EVENTS = "events";
	private Collection<TimetableEvent> events = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_unfold);
		FontApplicator fontApplicator = new FontApplicator(getAssets(), "Roboto-Light.ttf");
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		container.removeAllViews();
		
		if (getIntent() != null)
		{
			events = (Collection<TimetableEvent>) getIntent().getSerializableExtra(EXTRA_EVENTS);
			if (events != null)
			{
				for (TimetableEvent event : events)
					container.addView(event.getView(getLayoutInflater(), null, container, fontApplicator));
			}
		}
		
//		fontApplicator.applyFont(container);
	}

}
