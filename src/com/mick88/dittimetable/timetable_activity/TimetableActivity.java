package com.mick88.dittimetable.timetable_activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.PdfDownloaderService;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.downloader.Exceptions;
import com.mick88.dittimetable.downloader.TimetableDownloader;
import com.mick88.dittimetable.downloader.TimetableDownloader.TimetableDownloadListener;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.settings.SettingsActivity;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableStub;
import com.mick88.dittimetable.timetable_activity.GroupSelectionDialog.GroupSelectionListener;
import com.mick88.dittimetable.utils.FontApplicator;

public class TimetableActivity extends ActionBarActivity 
									implements GroupSelectionListener, TimetableDownloadListener
{
	private static class RetainedConfiguration
	{
		final TimetableDownloader downloader;
		final Timetable timetable;

		private RetainedConfiguration(TimetableDownloader downloader, Timetable timetable)
		{
			super();
			this.timetable = timetable;
			this.downloader = downloader;
		}
		
		public TimetableDownloader getDownloader()
		{
			return downloader;
		}
		
		public Timetable getTimetable()
		{
			return timetable;
		}
	}	
	
	public static final String EXTRA_ERROR_MESSAGE = "pdf_error_message";
	final int SETTINGS_REQUEST_CODE = 1;
	public static final String EXTRA_TIMETABLE = "timetable";
	
	final String logTag = "Timetable";
	Timetable timetable = null;
	// fragments to be refreshed
	Set<DayFragment> fragments = new HashSet<DayFragment>(5);
	private boolean timetableShown=false;
	
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
	
	public void addFragment(DayFragment fragment)
	{
		fragments.add(fragment);
	}
	
	public void removeFragment(DayFragment fragment)
	{
		fragments.remove(fragment);
	}
	
	private Timer timedUpdateTimer = null;
	private TimetableDownloader timetableDownloader;
       
    void downloadTimetable()
    {
    	if (timetableDownloader != null)
    		throw new RuntimeException("downloader already running");
    	showDownloadProgress();
    	timetableDownloader = new TimetableDownloader(getApplicationContext(), timetable, application.getSettings()).setTimetableDownloadListener(this);
    	timetableDownloader.execute();
    	supportInvalidateOptionsMenu();
    }
    
    void showDownloadProgress()
    {
    	showProgressPopup(getString(R.string.downloading_timetable_));
    }
    
    void showEmptyTimetableMessage()
    {
    	showMessage(false, getString(R.string.selected_timetable_is_empty), new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				showSettingsScreen(true);					
			}
		}, getString(R.string.settings));
    }
    
    @Override
    protected void onDestroy()
    {
    	if (timetableDownloader != null)
    	{
    		timetableDownloader.setTimetableDownloadListener(null);
    	}
    	super.onDestroy();
    }
    
	@Override
	public void onTimetableDownloaded(Timetable timetable,
			RuntimeException exception)
	{
		timetableDownloader = null;
		supportInvalidateOptionsMenu();
		showTimetable();		
		if (exception == null)
		{
			refresh();
			new DatabaseHelper(getApplicationContext()).saveTimetable(timetable);
		}
		else if (exception instanceof Exceptions.SettingsEmptyException)
		{
			showSettingsScreen(false);
		}
		else if (exception instanceof Exceptions.EmptyTimetableException)
		{
			showEmptyTimetableMessage();
		}
		else if (exception instanceof Exceptions.DownloadCancelledException)
		{
			DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
			timetable = databaseHelper.loadTimetable(getSettings());
			
			if (timetable == null)
			{
				this.timetable = new Timetable(getSettings());
				
				showMessage(false, getString(R.string.download_cancelled), new View.OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						downloadTimetable();					
					}
				}, getString(R.string.retry));
//				setStatusMessage(R.string.no_local_copy);
			}
			else 
			{
				setTimetable(timetable);
				refresh();
			}
			
		}
		else
		{
			showMessage(false, exception.getMessage(), new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					downloadTimetable();					
				}
			}, getString(R.string.retry));
		}
	}
	
	@Override
	public void onDownloadProgress(TimetableDownloader timetableDownloader, int progress, int max)
	{
		ProgressBar progressBar = (ProgressBar) findViewById(android.R.id.progress);
		progressBar.setIndeterminate(false);
		progressBar.setMax(max);
		progressBar.setProgress(progress);
		
		if (max > 0)
		{
			if (timetableDownloader.getStatusMessage() == R.string.downloading_event_details)
			{
				// Show X of X progress message
				setStatusMessage(getString(R.string.downloading_event_details_progress)
						.replace("{current}", String.valueOf(progress))
						.replace("{total}", String.valueOf(max)));
			}
		}
	}
    
    public void setTitle()
    {
    	ActionBar actionBar = getSupportActionBar(); 	
    	
    	actionBar.setTitle(timetable.describe());
    	actionBar.setSubtitle(timetable.describeWeeks());
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
    	TimetablePageAdapter timetablePageAdapter = new TimetablePageAdapter(getSupportFragmentManager());
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
		else if (extras != null && extras.containsKey(EXTRA_TIMETABLE))
		{
			Object extraTimetable = extras.getSerializable(EXTRA_TIMETABLE);
			if (extraTimetable instanceof Timetable)
			{
				this.timetable = (Timetable) extraTimetable;
				refresh();
			}
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
						.getCurrentSemester() == 1) ? Timetable.SEMESTER_1
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
				if (weekRange == Timetable.INVALID_WEEK_RANGE) setTimetable(new Timetable(course, year, weeks));
				else setTimetable(new Timetable(course, year, weekRange));
				
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
		
		setContentView(R.layout.fragment_timetable);

		showMessage(true, getString(R.string.loading_), null, null);
		
		FontApplicator fontApplicator = new FontApplicator(getAssets(), TimetableApp.FONT_NAME);
		fontApplicator.applyFont(getWindow().getDecorView());
		application = (TimetableApp) getApplication();
		
		Object retainedInstance = getLastCustomNonConfigurationInstance();
		if (retainedInstance instanceof RetainedConfiguration)
		{
			RetainedConfiguration configuration = (RetainedConfiguration) retainedInstance;
			this.timetable = configuration.getTimetable();
			this.timetableDownloader = configuration.getDownloader();
			if (timetableDownloader != null)
			{
				showDownloadProgress();
				if (timetableDownloader.getProgressMax() != 0)
					onDownloadProgress(timetableDownloader, timetableDownloader.getProgressCurrent(), timetableDownloader.getProgressMax());
				setStatusMessage(timetableDownloader.getStatusMessage());
				timetableDownloader.setTimetableDownloadListener(this);
			}
			
		}
		else processIntent();
				
		if (timetable == null) // if intent isnt processed
		{
			if (application.getSettings().isCourseDataSpecified() == false)
			{
				showSettingsScreen(false);
			}
			else openTimetable(getSettings());
		}
		setupViewPager();
		setupActionBar();
	}
	
	@Override
	public void onBackPressed()
	{
		if (this.timetableDownloader != null)
			cancelDownload();
		else if (timetableShown == false)
			showTimetable();
		else
			super.onBackPressed();
	}
	
	void setupActionBar()
	{
		new AsyncTask<Void, Void, List<TimetableStub>>()
		{

			@Override
			protected List<TimetableStub> doInBackground(Void... params)
			{
				long t = System.currentTimeMillis();
				List<TimetableStub> timetables = new DatabaseHelper(getApplicationContext()).getSavedTimetables();
				if (timetables.contains(timetable) == false) timetables.add(timetable);
				Collections.sort(timetables);
				t = System.currentTimeMillis() - t;
				Log.d("Timetable activity", "Timetables loaded in "+String.valueOf(t));
				return timetables;
			}
			
			@Override
			protected void onPostExecute(final List<TimetableStub> timetables)
			{
				if (timetables.size() == 1) return;
				getSupportActionBar().setDisplayShowTitleEnabled(false);
				
				Context context = new ContextThemeWrapper(TimetableActivity.this, R.style.Theme_AppCompat);
				getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
				TimetableDropdownAdapter timetableAdapter = new TimetableDropdownAdapter(context, timetables);
				timetableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				getSupportActionBar().setListNavigationCallbacks(timetableAdapter, new OnNavigationListener()
				{			
					@Override
					public boolean onNavigationItemSelected(int arg0, long arg1)
					{
						if (timetables.get(arg0).equals(timetable) == false)
						{
							onTimetableDropdownSelected(timetables.get(arg0));
							return true;
						}
						else return false;
					}
				});

				getSupportActionBar().setSelectedNavigationItem(timetables.indexOf(timetable));
			}
		}.execute();
	}
	
	void onTimetableDropdownSelected(TimetableStub timetable)
	{
		if (timetableDownloader != null)
		{
			cancelDownload();
		}		
		
		new AsyncTask<TimetableStub, Void, Timetable>()
		{
			@Override
			protected void onPreExecute()
			{
				showProgressPopup(getString(R.string.loading_));
				super.onPreExecute();
			}
			@Override
			protected Timetable doInBackground(TimetableStub... params)
			{
				params[0].setAsDefault(getApplicationContext(), getSettings());
				return new DatabaseHelper(getApplicationContext()).loadTimetable(params[0]);
			}
			
			@Override
			protected void onPostExecute(Timetable result) 
			{
				if (result == null)
				{
					openTimetable(getSettings());
				}
				else
				{
					setTimetable(result);
					refresh();
				}
			}			
		}.execute(timetable);	
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
			for (DayFragment dayFragment : fragments)
				dayFragment.refresh();
			showTimetable();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void showTimetable()
	{
		if (timetableDownloader != null)
			throw new RuntimeException("Cannot show timetable while downloading");
		timetableShown=true;
		if (timetable.isEmpty())
		{
			showEmptyTimetableMessage();
		}
		else
		{
			findViewById(R.id.pager).setVisibility(View.VISIBLE);
			findViewById(R.id.layoutMessage).setVisibility(View.GONE);
		}
	}
	
	void showMessage(boolean showProgress, CharSequence message, android.view.View.OnClickListener buttonListener, CharSequence buttonText)
	{
		timetableShown = false;
		ProgressBar progressBar = (ProgressBar) findViewById(android.R.id.progress);
		progressBar.setVisibility(showProgress?View.VISIBLE:View.GONE);
		progressBar.setIndeterminate(true);
		setStatusMessage(0);
		TextView 
			tvMessage = (TextView) findViewById(R.id.tvMessage),
			btnMessageAction = (TextView) findViewById(R.id.btnMessageAction);
		
		if (message == null)
			tvMessage.setVisibility(View.GONE);
		else 
		{
			tvMessage.setVisibility(View.VISIBLE);
			tvMessage.setText(message);
		}
		
		if (buttonListener == null)
			btnMessageAction.setVisibility(View.GONE);
		else
		{
			btnMessageAction.setVisibility(View.VISIBLE);
			btnMessageAction.setText(buttonText);
			btnMessageAction.setOnClickListener(buttonListener);
		}
		
		findViewById(R.id.layoutMessage).setVisibility(View.VISIBLE);
		findViewById(R.id.pager).setVisibility(View.GONE);
	}
	
	AppSettings getSettings()
	{
		return application.getSettings();
	}

	
	public void setTimetable(Timetable timetable)
	{
		this.timetable = timetable;
		
		if (timetable.isCourseDataSpecified()) 
		{
			setTitle();
		}
	}
	
	void openTimetable(AppSettings appSettings)
	{
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		Timetable timetable = databaseHelper.loadTimetable(appSettings);
		if (timetable == null) 
		{
			timetable = new Timetable(appSettings);
			try
			{
				timetable.importSavedTimetable(getApplicationContext());
				databaseHelper.saveTimetable(timetable);
			}
			catch (Exceptions.NoLocalCopyException e)
			{
				this.timetable = timetable;
				downloadTimetable();
			}
		}
		setTimetable(timetable);
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
	public Object onRetainCustomNonConfigurationInstance()
	{
		return new RetainedConfiguration(timetableDownloader, this.timetable);
	}
	
	void cancelDownload()
	{
		if (timetableDownloader != null)
		{
			setStatusMessage(R.string.cancelling_);
			timetableDownloader.cancel(true);
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
			
		case R.id.menu_refresh_cancel:
			cancelDownload();
			return true;
			
		case R.id.menu_refresh_timetable:
			try
			{
				onBtnRefreshPressed();
			}
			catch (Exception e)
			{
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
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
			intent.putExtra(Intent.EXTRA_TEXT, timetable.toString(application.getSettings()));
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
		if (timetableDownloader != null)
		{
			menu.findItem(R.id.menu_refresh_cancel).setVisible(true);
			menu.findItem(R.id.menu_refresh_timetable).setVisible(false);
		}
		return true;
	}
	
	void showProgressPopup(CharSequence message)
	{
		showMessage(true, message, null, null);
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
	
	public Timetable getTimetable()
	{
		return timetable;
	}
	
	void setStatusMessage(int stringResourceId)
	{
		TextView tvStatus = (TextView) findViewById(R.id.tvDownloadStatus);
		tvStatus.setVisibility(stringResourceId == 0 ? View.GONE : View.VISIBLE);
		if (stringResourceId != 0)
		{
			tvStatus.setText(stringResourceId);
		}
	}
	
	void setStatusMessage(String message)
	{
		TextView tvStatus = (TextView) findViewById(R.id.tvDownloadStatus);
		tvStatus.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
		if (TextUtils.isEmpty(message) == false)
		{
			tvStatus.setText(message);
		}
	}

	@Override
	public void onStatusChange(TimetableDownloader downloader)
	{
		setStatusMessage(downloader.getStatusMessage());
		
	}
}
