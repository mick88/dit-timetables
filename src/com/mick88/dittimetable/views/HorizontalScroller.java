package com.mick88.dittimetable.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HorizontalScroller extends HorizontalScrollView
{
	ViewPager parentView;

	public HorizontalScroller(Context context) {
		super(context);
	}

	public HorizontalScroller setParent(ViewPager parentView)
	{
		this.parentView = parentView;
		return this;
	}

	@Override
	public synchronized boolean onTouchEvent(MotionEvent event)
	{
		if ((event.getAction() == MotionEvent.ACTION_DOWN)
				&& (parentView != null))
		{
			parentView.requestDisallowInterceptTouchEvent(true);
			Log.i(toString(), "Touch event interception disallowed!");
		}

		return super.onTouchEvent(event);
	}
}
