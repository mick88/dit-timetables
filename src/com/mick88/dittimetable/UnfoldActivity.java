package com.mick88.dittimetable;

import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.mick88.dittimetable.event_details.EventDetailsSwipableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.event_list.SingleEventItem;
import com.mick88.dittimetable.utils.FontApplicator;

public class UnfoldActivity extends Activity implements OnClickListener
{
	/**
	 * Extra argument containing a List of TimetableEvents
	 * as a Serializable object
	 */
	public static final String EXTRA_EVENTS = "events";
	
	/**
	 * Extra argument containing a List starting positions to all events
	 */
	public static final String EXTRA_POSITIONS = "positions";
	
	public static final String EXTRA_TIMETABLE = "timetable";
	
	private List<TimetableEvent> events = null;
	private Timetable timetable = null;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unfold);
		FontApplicator fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		timetable = (Timetable) getIntent().getExtras().getSerializable(EXTRA_TIMETABLE);
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		container.removeAllViews();
		
		if (getIntent() != null)
		{
			events = (List<TimetableEvent>) getIntent().getSerializableExtra(EXTRA_EVENTS);
			List<Integer> positions = getIntent().getExtras().getIntegerArrayList(EXTRA_POSITIONS);
			if (events != null)
			{
				int i=0;
				for (final TimetableEvent event : events)
				{
					View view = SingleEventItem.instantiateForEvent(event).getView(getLayoutInflater(), null, container, fontApplicator, false, timetable);
					view.setOnClickListener(new OnClickListener()
					{
						
						@Override
						public void onClick(View v)
						{
							TimetableApp application = (TimetableApp) getApplication();
							Intent intent = new Intent(getApplicationContext(), EventDetailsSwipableActivity.class);
							intent.putExtra(EventDetailsSwipableActivity.EXTRA_SELECTED_EVENT, event);
							intent.putExtra(EventDetailsSwipableActivity.EXTRA_DAY, timetable.getDay(event.getDay()));
							startActivity(intent);
						}
					});
					container.addView(view);
//					animateTile(view, positions.get(i));										
					i++;
				}
				
			}
		}
		
		container.setOnClickListener(this);
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	void animateTile(View view, int fromY)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
		
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(view, View.Y, fromY));
		set.setDuration(500);
		set.start();
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
