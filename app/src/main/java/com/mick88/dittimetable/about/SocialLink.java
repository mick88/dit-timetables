package com.mick88.dittimetable.about;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.mick88.dittimetable.about.SocialLinkAdapter.SocialLinkItem;

public class SocialLink implements SocialLinkItem
{
	private final int imageResource, stringResource;
	private final String url;
	
	public SocialLink(int imageResource, int stringResource, String url)
	{
		this.imageResource = imageResource;
		this.stringResource = stringResource;
		this.url = url;
	}

	@Override
	public void setText(TextView textView)
	{
		textView.setText(stringResource);		
	}

	@Override
	public void setImage(ImageView imageView)
	{
		imageView.setImageResource(imageResource);
		
	}
	
	public Uri getUri()
	{
		return Uri.parse(getUrl());
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public String getText(Context context)
	{
		return context.getString(stringResource);
	}

}
