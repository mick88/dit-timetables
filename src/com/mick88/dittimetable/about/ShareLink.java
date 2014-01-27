package com.mick88.dittimetable.about;

import android.content.Intent;

import com.mick88.dittimetable.R;

public class ShareLink extends SocialLink
{

	public ShareLink(int stringResource, String url)
	{
		super(R.drawable.ic_share_btn, stringResource, url);
	}
	
	public Intent getIntent()
	{
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, getUrl());
		shareIntent.putExtra(Intent.EXTRA_SUBJECT,
				R.string.share_attach_text);
		return shareIntent;
	}

}
