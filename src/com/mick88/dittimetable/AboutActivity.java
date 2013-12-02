package com.mick88.dittimetable;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class AboutActivity extends ActionBarActivity implements OnClickListener
{
	private static final String TWITTER_URL = "https://twitter.com/michaldabski";
	private static final String GOOGLE_PLUS_URL = "https://plus.google.com/u/0/+MichalDabski";
	private static final String WEBSITE_URL = "http://www.michaldabski.com/";
	private static final String FEEDBACK_URI = "mailto:contact@michaldabski.com";
	private static final String PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.mick88.dittimetable";
	public static final String EXTRA_TIMETABLE_INFO = "extra_timetable_info";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		// Show the Up button in the action bar.
		setupActionBar();
		
		for (int btnResource : new int[]{
				R.id.btnShare, 
				R.id.btnFeedback,
				R.id.btnWebsite,
				R.id.btnTwitter,
				R.id.btnGooglePlus,
				})
			findViewById(btnResource).setOnClickListener(this);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnShare:
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, PLAYSTORE_URL);
				shareIntent.putExtra(Intent.EXTRA_SUBJECT,
						R.string.share_attach_text);
				startActivity(Intent.createChooser(shareIntent,
						getString(R.string.share_app)));
				break;
			
			case R.id.btnFeedback:
				CharSequence timetableDescription = "";
				try
				{
					timetableDescription = getIntent().getCharSequenceExtra(
							EXTRA_TIMETABLE_INFO);
				} catch (Exception e)
				{
				}
				Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO,
						Uri.parse(FEEDBACK_URI));
				feedbackIntent.putExtra(Intent.EXTRA_SUBJECT,
						"DIT Timetable feedback");
				feedbackIntent.putExtra(
						Intent.EXTRA_TEXT,
						getString(R.string.feedback_preset_string, Build.BRAND,
								Build.MODEL, Build.VERSION.RELEASE,
								Build.VERSION.SDK_INT, timetableDescription));
				try
				{
					startActivity(feedbackIntent);
				}
				catch (ActivityNotFoundException e)
				{
					Toast.makeText(this, R.string.no_e_mail_client_detected, Toast.LENGTH_SHORT).show();
				}
				break;
			
			case R.id.btnWebsite:
				Intent websiteIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(WEBSITE_URL));
				startActivity(websiteIntent);
				break;
				
			case R.id.btnGooglePlus:
				Intent googlePlusIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(GOOGLE_PLUS_URL));
				startActivity(googlePlusIntent);
				break;
				
			case R.id.btnTwitter:
				Intent twitterIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(TWITTER_URL));
				startActivity(twitterIntent);
				break;
		}
		
	}
	
}
