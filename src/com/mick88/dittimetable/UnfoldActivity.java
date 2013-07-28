package com.mick88.dittimetable;

import java.util.Collection;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.utils.FontApplicator;

public class UnfoldActivity extends Activity implements OnClickListener
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
		setContentView(R.layout.activity_unfold);
		FontApplicator fontApplicator = new FontApplicator(getAssets(), "Roboto-Light.ttf");
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		container.removeAllViews();
		
		if (getIntent() != null)
		{
			events = (Collection<TimetableEvent>) getIntent().getSerializableExtra(EXTRA_EVENTS);
			if (events != null)
			{
				for (final TimetableEvent event : events)
				{
					View view = event.getView(getLayoutInflater(), null, container, fontApplicator);
					view.setOnClickListener(new OnClickListener()
					{
						
						@Override
						public void onClick(View v)
						{
							// TODO Auto-generated method stub							
						}
					});
					container.addView(view);
				}
				
			}
		}
		
		container.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.container)
		{
			finish();
		}
		
	}

}
