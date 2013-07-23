package com.mick88.dittimetable.swipable_tabs;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.mick88.dittimetable.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CustomPageTransformer 
	implements ViewPager.PageTransformer
{

	final float min_scale = 0.85f,
			min_alpha=0.7f;	
	
	@Override
	public void transformPage(View view, float position)
	{
		// animate each child separately
		/*if (view instanceof ViewGroup && ((ViewGroup) view).getChildAt(0) instanceof ViewGroup)
		{
			ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(0);
			for (int i=0; i  < viewGroup.getChildCount(); i++)
			{
				transformView(viewGroup.getChildAt(i), position);
			}
		}
		else */
			transformView(view.findViewById(R.id.dayContent), position);		
		
	}
	
	private void transformView(View view, float position)
	{
		if (view == null) return;
		int pageWidth = view.getWidth(),
			pageHeight = view.getHeight();
		
		if (position < -1)
		{
			view.setAlpha(0);
		}
		else if (position <= 1)
		{
			float scale = Math.max(min_scale, 1 - Math.abs(position));
			float verticalMargin = pageHeight * (1-scale) / 2;
			float horizontalMargin = pageWidth * (1 - scale) / 2;
			
			if (position < 0)
			{
				view.setTranslationX(horizontalMargin - verticalMargin / 2);
			}
			else
			{
				view.setTranslationX(-horizontalMargin + verticalMargin / 2);
			}
			
			view.setScaleX(scale);
			view.setScaleY(scale);
			
			view.setAlpha(min_alpha + (scale - min_scale) / (1-min_scale) * (1-min_alpha));
		}
		else
		{
			view.setAlpha(0);
		}
		
	}
	
}
