package com.mick88.dittimetable;

import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.mick88.dittimetable.timetable.TimetableEvent;
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
	
	private List<TimetableEvent> events = null;
	
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
			events = (List<TimetableEvent>) getIntent().getSerializableExtra(EXTRA_EVENTS);
			List<Integer> positions = getIntent().getExtras().getIntegerArrayList(EXTRA_POSITIONS);
			if (events != null)
			{
				int i=0;
				for (final TimetableEvent event : events)
				{
					View view = event.getView(getLayoutInflater(), null, container, fontApplicator, false);
					view.setOnClickListener(new OnClickListener()
					{
						
						@Override
						public void onClick(View v)
						{
							// TODO Auto-generated method stub							
						}
					});
					container.addView(view);
					animateTile(view, positions.get(i));										
					i++;
				}
				
			}
		}
		
		container.setOnClickListener(this);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void animateTile(View view, int fromY)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return;
		
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
