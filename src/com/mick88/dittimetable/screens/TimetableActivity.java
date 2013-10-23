package com.mick88.dittimetable.screens;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mick88.dittimetable.GroupSelectionDialog;
import com.mick88.dittimetable.GroupSelectionDialog.GroupSelectionListener;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.pdf_download.PdfDownloaderService;
import com.mick88.dittimetable.swipable_tabs.TimetablePageAdapter;
import com.mick88.dittimetable.timetable.Exceptions;
import com.mick88.dittimetable.timetable.Exceptions.NoLocalCopyException;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDownloader;
import com.mick88.dittimetable.timetable.TimetableDownloader.TimetableDownloadListener;
import com.mick88.dittimetable.utils.FontApplicator;

public class TimetableActivity extends ActionBarActivity 
									implements GroupSelectionListener, TabListener
{
	public static final String EXTRA_ERROR_MESSAGE = "pdf_error_message";
	final int SETTINGS_REQUEST_CODE = 1;
	public static final String EXTRA_TIMETABLE = "timetable";
	
	final String logTag = "Timetable";
	String html;
	TextView textView;
	Timetable timetable = null;
	ProgressDialog progressDialog=null;
	int currentWeek = Timetable.getCurrentWeek();
	
	TimetablePageAdapter timetablePageAdapter=null;
	ViewPager viewPager=null;
	
	/**
	 * true if user is just browsing - from intent
	 */
	boolean isTemporaryTimetable = false;
	
	TimetableApp application;
	
	final private Handler uiHandler = new Handler();
	
	void toast(CharSequence message)
	{
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	
	private Timer timedUpdateTimer = null;
       
    void downloadTimetable()
    {
    	showProgressPopup("Downloading timetable...");
    	TimetableDownloader timetableDownloader = new TimetableDownloader(getApplicationContext(), timetable).setTimetableDownloadListener(new TimetableDownloadListener()
		{
			
			@Override
			public void onTimetableDownloaded(Timetable timetable,
					RuntimeException exception)
			{
				dismissProgresPopup();
				if (exception == null)
				{
					refresh();
				}
				else if (exception instanceof Exceptions.SettingsEmptyException)
				{
					showSettingsScreen(false);
				}
				else
				{
					new AlertDialog.Builder(TimetableActivity.this)
						.setMessage(exception.getMessage())
						.setPositiveButton(android.R.string.ok, null)
						.show();
				}
			}
			
			@Override
			public void onDownloadProgress(int progress, int max)
			{
				onProgress(progress, max);
			}
		});
    	timetableDownloader.execute();
    }
    
    public void setTitle()
    {
    	ActionBar actionBar = getSupportActionBar(); 	
    	
    	actionBar.setTitle(timetable.describe());
    	actionBar.setSubtitle(timetable.describeWeeks());
    }
    
    void loadTimetable()
    {
    	try
    	{
    		timetable.importSavedTimetable(getApplicationContext());
    	}
    	catch (NoLocalCopyException e)
    	{
    		downloadTimetable();
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    }
    
    public ViewPager getViewPager()
	{
		return viewPager;
	}
    
    void setupViewPager()
    {
    	timetablePageAdapter = new TimetablePageAdapter(getSupportFragmentManager(), timetable);
    	viewPager  = (ViewPager) findViewById(R.id.pager);
    	viewPager.setAdapter(timetablePageAdapter);
    	showToday(false);
    }
    
    void showToday(boolean smooth)
    {
    	viewPager.setCurrentItem(Timetable.getTodayId(true), smooth);
    }
    
    public Map<String, String> parseQuery(String query)
    {
    	HashMap<String, String> result = new HashMap<String, String>();
    	
    	String [] vars = query.split("&");
    	for (String var : vars)
    	{
    		String [] keyValue = var.split("=", -1);
    		if (keyValue.length == 2)
    		{
    			result.put(keyValue[0], keyValue[1]);
    		}
    	}
    	
    	return result;
    }
    
	void processIntent()
	{
		Intent intent = getIntent();

		Uri address = intent.getData();
		
		Bundle extras = intent.getExtras();
		if (extras != null && extras.containsKey(EXTRA_ERROR_MESSAGE))
		{
			new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage(extras.getString(EXTRA_ERROR_MESSAGE))
				.setPositiveButton(android.R.string.ok, null)
				.show();
		}

		if (address != null && address.getHost().equalsIgnoreCase("www.dit.ie"))
		{
			String data = address.getEncodedQuery();

			try
			{
				Map<String, String> values = parseQuery(data);
				// https://www.dit.ie/timetables/PortalServ?reqtype=timetable&ttType=COURSE&sKey=201213|DT211&sYear=2&weekRange=4603
				// 201213%7CDT211
				if (values.get("reqtype").equalsIgnoreCase("timetable") == false)
				{
					return;
				}

				String course = values.get("sKey").split("%7C")[1], 
						weeks = (Timetable
						.getSemester() == 1) ? Timetable.SEMESTER_1
						: Timetable.SEMESTER_2;
				int weekRange = Timetable.INVALID_WEEK_RANGE;
				try
				{
					Integer.parseInt(values.get("weekRange"));
				}
				catch (Exception e)
				{
					
				}
				
				int year = Integer.parseInt(values.get("sYear"));

				if (TextUtils.isEmpty(course) || year < 1 || year > 4)
				{
					return;
				}
				if (weekRange == Timetable.INVALID_WEEK_RANGE) setTimetable(new Timetable(course, year, weeks, application.getSettings()));
				else setTimetable(new Timetable(course, year, weekRange, application.getSettings()));
				
//				loadTimetable();
				isTemporaryTimetable = true;
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			} catch (Exception e)
			{
				e.printStackTrace();
				return;
			}

			
		}
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_timetable);

		FontApplicator fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		fontApplicator.applyFont(getWindow().getDecorView());
		application = (TimetableApp) getApplication();
		
		if (savedInstanceState != null)
		{
			Object extraTimetable = savedInstanceState.getSerializable(EXTRA_TIMETABLE);
			if (extraTimetable instanceof Timetable)
			{
				this.timetable = (Timetable) extraTimetable;
				refresh();
			}
		}
		else processIntent();
				
		if (timetable == null) // if intent isnt processed
		{
			setTimetable(new Timetable(application.getSettings()));

			if (application.getSettings().isCourseDataSpecified() == false)
			{
				showSettingsScreen(false);
			}
		}
		setupViewPager();

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRA_TIMETABLE, this.timetable);
	}
	
	/*
	 * Schedules updated for the next full hour
	 */
	private void scheduleTabUpdate()
	{
		timedUpdateTimer = new Timer();
		
		Calendar nextUpdate = Calendar.getInstance();
		nextUpdate.set(Calendar.MINUTE, 0);
		nextUpdate.set(Calendar.SECOND, 0);
		nextUpdate.add(Calendar.HOUR, 1);
		
		Log.d(logTag, "Update scheduled for "+nextUpdate.getTime());
		
		timedUpdateTimer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
				Log.d(logTag, "Update timer executed");
				uiHandler.post(new Runnable()
				{

					@Override
					public void run()
					{
						refresh();
						scheduleTabUpdate();
					}
				});
			}
		}, nextUpdate.getTime());
		
		
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		refresh();
		scheduleTabUpdate();
	};
	
	@Override
	protected void onStart()
	{
		FlurryAgent.onStartSession(this, TimetableApp.FLURRY_API_KEY);
		Map<String, String> data = TimetableApp.getDeviceData();
		if (timetable != null)
		{
			data.putAll(timetable.ToHashMap());
		}
		FlurryAgent.onEvent("Timetable app started", data);
		super.onStart();
	}
	
	@Override
	protected void onStop()
	{
		FlurryAgent.onEndSession(this);
		super.onStop();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		timedUpdateTimer.cancel();
	}
	
	void refresh()
	{
		try
		{
			setTitle();
			timetablePageAdapter.setTimetable(timetable);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	public void setTimetable(Timetable timetable)
	{
		if (this.timetable != null) this.timetable.dispose();
		this.timetable = timetable;
		
		if (this.timetable.getSettings().isCourseDataSpecified()) 
		{
			setTitle();
			loadTimetable();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case SETTINGS_REQUEST_CODE:
			if (resultCode == RESULT_OK)
			{
				setTimetable(new Timetable(application.getSettings()));
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	void downloadPdfInService()
	{
		Intent intent = new Intent(getApplicationContext(), PdfDownloaderService.class);
		intent.putExtra(PdfDownloaderService.EXTRA_TIMETABLE, this.timetable);
		startService(intent);
	}
	
	void showSettingsScreen(boolean allowCancel)
	{
		if (allowCancel == false)
			toast("Please fill the settings before using the app.");
		Intent settingsScreen = new Intent(TimetableActivity.this, SettingsActivity.class);
		settingsScreen.putExtra(SettingsActivity.EXTRA_ALLOW_CANCEL, allowCancel);
		startActivity(settingsScreen);
		finish();
	}
	
	void showGroupSelectionDialog()
	{
		Set<String> groups = timetable.getGroupsInTimetable();
		
		if (groups.isEmpty())
		{
			new AlertDialog.Builder(this)
			.setTitle(R.string.app_name)
			.setMessage("No groups detected in current timetable")
			.setNeutralButton(android.R.string.ok, null)
			.setNegativeButton("Reload", new OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					onBtnRefreshPressed();
//					refresh();
				}
			})
			.setIcon(R.drawable.ic_launcher)
			.show();
		}
		else
		{
			List<String> gList = new ArrayList<String>(groups);
			Collections.sort(gList);
			String[] gArray = new String[gList.size()];
			gList.toArray(gArray);
			
			groups = application.getSettings().getHiddenGroups();
			String[] hiddenGroups = new String[groups.size()];
			groups.toArray(hiddenGroups);
			
			GroupSelectionDialog dialog = new GroupSelectionDialog();
			dialog.setGroups(gArray, hiddenGroups);
			dialog.show(getSupportFragmentManager(), "GroupSelector");
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			if (isTemporaryTimetable)
			{
				Intent intent = new Intent(getApplicationContext(), TimetableActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			}
			return false;
			
		case R.id.menu_download_pdf:			
//			downloadPdf();
			downloadPdfInService();
			return true;
			
		case R.id.menu_refresh_timetable:
			onBtnRefreshPressed();	
			return true;
			
		case R.id.menu_settings:
			showSettingsScreen(true);
			FlurryAgent.onEvent("Settings screen open");
			return true;
			
		case R.id.menu_groups:
			showGroupSelectionDialog();
			return true;
			
		case R.id.share_timetable:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, timetable.toString());
			startActivity(Intent.createChooser(intent, "Share timetable via..."));
			FlurryAgent.onEvent("Share timetable");
			return true;
			
		/*case R.id.menu_web:
			new Thread (new Runnable()
			{
				
				@Override
				public void run()
				{
					final String url = timetable.getQueryAddress();
					final Bundle cookie = timetable.getConnection().getCookie().getBundle();
					final Uri uri = Uri.parse(String.format(Locale.getDefault(), "%s%s", Connection.WEBSITE_ADDRESS, url));
					uiHandler.postDelayed(new Runnable()
					{
						
						@Override
						public void run()
						{
							Intent intent = new Intent(Intent.ACTION_VIEW,  uri);
							intent.putExtra(Browser.EXTRA_HEADERS, cookie);
							startActivity(Intent.createChooser(intent, "Open in web browser:"));
							
						}
					}, 100);
					
					
					
				}
			}).start();
			return true;*/
			
		case R.id.menu_about:
			AlertDialog dialog = new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("About Timetables")
				.setMessage(R.string.about_text)
				.setPositiveButton(android.R.string.ok, null)
				.setNeutralButton("Feedback", new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:contact@michaldabski.com"));
						intent.putExtra(Intent.EXTRA_SUBJECT, "DIT Timetable feedback");
						intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.ENGLISH, "Device: %s %s.\nAndroid %s, API %d.\nDataset: %s\n\n", 
								Build.BRAND, Build.MODEL,
								Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
								timetable.describe()));
						startActivity(intent);						
					}
				})
				.setNegativeButton("Share", new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.mick88.dittimetable");
						intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!");
						startActivity(Intent.createChooser(intent, "Share app"));
						
					}
				})
				.create();
			dialog.show();
			FlurryAgent.onEvent("About Screen");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	void onBtnRefreshPressed()
	{
		downloadTimetable();
		FlurryAgent.onEvent("Timetable refreshed by user");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_timetable, menu);
		return true;
	}
	
	void showProgressPopup(CharSequence message)
	{
		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(1);
		progressDialog.setProgress(0);
		progressDialog.setTitle(R.string.app_name);
		progressDialog.setMessage(message); 
		progressDialog.setIcon(R.drawable.ic_launcher);
		progressDialog.setCancelable(false);
		
		progressDialog.show();
	}
	
	void showPopupMessage(CharSequence message)
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage(message)
				.setNeutralButton("OK", null)
				.setIcon(R.drawable.ic_launcher)
				.show();
	}
	
	void dismissProgresPopup()
	{
		try
		{
			if (progressDialog != null)
			{
				progressDialog.dismiss();
				progressDialog = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onGroupsSelected(
			ArrayList<String> selected,
			ArrayList<String> unselected)
	{
		for (String s : selected) application.getSettings().unhideGroup(s);
		for (String s : unselected) application.getSettings().hideGroup(s);
		application.getSettings().saveSettings(this);
		refresh();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		if (viewPager != null) viewPager.setCurrentItem(tab.getPosition(), true);
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
		
	}
	
	public Timetable getTimetable()
	{
		return timetable;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
		
	}

	public void onProgress(final int position, final int max)
	{
		Log.d(logTag, "Progress update "+String.valueOf(position)+"/"+String.valueOf(max));
		if (progressDialog == null) showProgressPopup("Downloading timetable...");
		if (max >= position)
		{
			if (progressDialog != null)
			{
				progressDialog.setIndeterminate(false);
				progressDialog.setMax(max);
				progressDialog.setProgress(position);
			}
			
		}
		
	}
}
