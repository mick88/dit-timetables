package com.mick88.dittimetable.about;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.about.SocialLinkAdapter.SocialLinkItem;
import com.mick88.dittimetable.utils.FontApplicator;

public class AboutActivity extends ActionBarActivity implements OnItemClickListener
{
	private static final String ANDROID_APPS_URL = "https://play.google.com/store/apps/developer?id=mick88";
	private static final String TWITTER_URL = "https://twitter.com/michaldabski";
	private static final String GOOGLE_PLUS_URL = "https://plus.google.com/u/0/+MichalDabski";
	private static final String WEBSITE_URL = "http://www.michaldabski.com/";
	private static final String FEEDBACK_URI = "mailto:contact@michaldabski.com";
	private static final String SHARE_URL = "https://play.google.com/store/apps/details?id=com.mick88.dittimetable";
	public static final String EXTRA_TIMETABLE_INFO = "extra_timetable_info";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		// Show the Up button in the action bar.
		setupActionBar();
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		
		View header = getLayoutInflater().inflate(R.layout.about_header, listView, false);
		listView.addHeaderView(header);
		listView.setOnItemClickListener(this);
		
		listView.setAdapter(new SocialLinkAdapter(this, new SocialLinkItem[]{
				new FeedbackLink(),
				new ShareLink(R.string.share_app, SHARE_URL),
				new SocialLink(R.drawable.ic_website, R.string.visit_website, WEBSITE_URL),
				new SocialLink(R.drawable.ic_android_apps, R.string.other_apps, ANDROID_APPS_URL),
				new SocialLink(R.drawable.ic_google_plus, R.string.google_plus, GOOGLE_PLUS_URL),
				new SocialLink(R.drawable.ic_twitter, R.string.twitter, TWITTER_URL),
		}));
		
		FontApplicator fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		fontApplicator.applyFont(getWindow().getDecorView());
		fontApplicator.applyFont(header);
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		Object clickedObject = arg0.getItemAtPosition(arg2);
		if (clickedObject instanceof ShareLink)
		{
			startActivity(Intent.createChooser(((ShareLink) clickedObject).getIntent(),
					getString(R.string.share_app)));
		}
		else if (clickedObject instanceof SocialLink)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(((SocialLink) clickedObject).getUrl()));
			startActivity(intent);
		}
		else if (clickedObject instanceof FeedbackLink)
		{
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
					getString(R.string.dit_timetable_feedback));
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
		}
		
	}
	
}
