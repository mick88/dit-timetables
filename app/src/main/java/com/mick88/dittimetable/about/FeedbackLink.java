package com.mick88.dittimetable.about;

import android.widget.ImageView;
import android.widget.TextView;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.about.SocialLinkAdapter.SocialLinkItem;

public class FeedbackLink implements SocialLinkItem
{

	@Override
	public void setText(TextView textView)
	{
		textView.setText(R.string.feedback);		
	}

	@Override
	public void setImage(ImageView imageView)
	{
		imageView.setImageResource(R.drawable.ic_feedback);
		
	}

}
