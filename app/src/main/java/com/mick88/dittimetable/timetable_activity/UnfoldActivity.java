package com.mick88.dittimetable.timetable_activity;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.RelativeLayout;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.event_details.EventDetailsSwipableActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable_activity.event_list.SingleEventItem;
import com.mick88.dittimetable.utils.FontApplicator;

import java.util.List;

public class UnfoldActivity extends Activity implements OnClickListener
{
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class SkippableInterpolator implements TimeInterpolator
	{
		private float skipOffset;
		public SkippableInterpolator(int skipMilliseconds, int duration)
		{
			this.skipOffset = (float) skipMilliseconds / (float) duration;
		}

		@Override
		public float getInterpolation(float input)
		{
			float result = input + skipOffset;
			return result < 1f ? result : 1f;
		}
		
	}
	
	private static final int 
		ANIMATION_IN_DURATION = 400,
		ANIMATION_OUT_DURATION = ANIMATION_IN_DURATION / 2;
	
	/**
	 * Extra argument containing a List of TimetableEvents
	 * as a Serializable object
	 */
	public static final String EXTRA_EVENTS = "events";	
	public static final String EXTRA_TIMETABLE = "timetable";
	public static final String EXTRA_OFFSET = "offset";
	public static final String EXTRA_START_AT = "start_at";
	
	private List<TimetableEvent> events = null;
	private Timetable timetable = null;
	private int initialCardPositionOffset;
	private int spaceBetweenCards;
	private boolean enableAnimations = true;
	private boolean isClosing=false;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unfold);
		FontApplicator fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		timetable = (Timetable) getIntent().getExtras().getSerializable(EXTRA_TIMETABLE);
		ViewGroup container = (ViewGroup) findViewById(R.id.container);
		this.enableAnimations = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		
		this.spaceBetweenCards = (int) getResources().getDimension(R.dimen.multievent_offset);
		
		if (getIntent() != null)
		{
			events = (List<TimetableEvent>) getIntent().getSerializableExtra(EXTRA_EVENTS);
			TransitionDrawable transitionDrawable = (TransitionDrawable) container.getBackground();
			transitionDrawable.startTransition(ANIMATION_IN_DURATION);
			this.initialCardPositionOffset = getIntent().getIntExtra(EXTRA_OFFSET, 0) - spaceBetweenCards;
			int margin=(int) getResources().getDimension(R.dimen.event_card_height);
			final long startAt = getIntent().getLongExtra(EXTRA_START_AT, System.currentTimeMillis());
			
			if (events != null)
			{
				for (int i=events.size()-1; i >= 0; i--)
				{
					final TimetableEvent event = events.get(i) ;
					View view = SingleEventItem.instantiateForEvent(event, timetable).getView(getLayoutInflater(), null, container, fontApplicator, true, timetable);
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
					params.setMargins(0, margin*i, 0, 0);
					view.setLayoutParams(params);
					view.setOnClickListener(new OnClickListener()
					{
						
						@Override
						public void onClick(View v)
						{
							Intent intent = new Intent(getApplicationContext(), EventDetailsSwipableActivity.class);
							intent.putExtra(EventDetailsSwipableActivity.EXTRA_SELECTED_EVENT, event);
							intent.putExtra(EventDetailsSwipableActivity.EXTRA_DAY, timetable.getDay(event.getDay()));
							startActivity(intent);
						}
					});
					container.addView(view);

					if (savedInstanceState == null && enableAnimations) animateCardIn(view, calcualteCardOriginY(i), (int)(System.currentTimeMillis()-startAt));
				}
			}
		}
		
		container.setOnClickListener(this);
	}
	
	int calcualteCardOriginY(int n)
	{
		return initialCardPositionOffset + (n*spaceBetweenCards);
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	void animateCardOut(final View view, final int toY, final Runnable onAnimEnd)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;

		final int fromY;
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		fromY = location[1] - spaceBetweenCards;
		
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(view, View.Y, fromY, toY));
		set.setDuration(ANIMATION_OUT_DURATION);
		set.addListener(new AnimatorListener()
		{
			
			@Override
			public void onAnimationStart(Animator animation)
			{

			}
			
			@Override
			public void onAnimationRepeat(Animator animation)
			{

			}
			
			@Override
			public void onAnimationEnd(Animator animation)
			{
				onAnimEnd.run();				
			}
			
			@Override
			public void onAnimationCancel(Animator animation)
			{

			}
		});
		set.start();		
	}
	
	void closeActivity()
	{
		if (isClosing) return;
		if (enableAnimations)
		{
			ViewGroup container = (ViewGroup) findViewById(R.id.container);
			TransitionDrawable transitionDrawable = (TransitionDrawable) container.getBackground();
			transitionDrawable.reverseTransition(ANIMATION_OUT_DURATION);
			int maxChildId = container.getChildCount()-1;
			for (int i=maxChildId; i >= 0 ; i--)
			{
				int j = maxChildId-i;
				View child = container.getChildAt(i);
				animateCardOut(child, calcualteCardOriginY(j), new Runnable()
				{
					
					@Override
					public void run()
					{
						finish();					
					}
				});
			}
		}
		else finish();		
		isClosing=true;
	}
	
	@Override
	public void onBackPressed()
	{		
		closeActivity();
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	void animateCardIn(final View view, final int fromY, final int skipMilliseconds)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
		Log.d("View from Y", String.valueOf(fromY));
		view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener()
		{
			
			@Override
			public boolean onPreDraw()
			{
				view.getViewTreeObserver().removeOnPreDrawListener(this);
				final int toY;
				int[] location = new int[2];
				view.getLocationOnScreen(location);
				toY = location[1];
				
				AnimatorSet set = new AnimatorSet();
				set.play(ObjectAnimator.ofFloat(view, View.Y, fromY, toY));
				set.setDuration(ANIMATION_IN_DURATION);
				set.setInterpolator(new SkippableInterpolator(skipMilliseconds, ANIMATION_IN_DURATION));
				set.start();
				return true;
			}
		});

		
	}
	
	@Override
	public void finish()
	{
		super.finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.container)
		{
			closeActivity();
		}
		
	}

}
