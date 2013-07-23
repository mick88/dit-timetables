package screens;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mick88.dittimetable.Connection;
import com.mick88.dittimetable.GroupSelectionDialog;
import com.mick88.dittimetable.GroupSelectionDialog.GroupSelectionListener;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.Timetable.ErrorCode;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;

public class TimetableActivity extends SherlockFragmentActivity 
									implements Timetable.ResultHandler, GroupSelectionListener
{

	final int SETTINGS_REQUEST_CODE = 1;
	final String AD_UNIT_ID = "a150fece64db26c";
	
	final String logTag = "Timetable";
	String html;
	TextView textView;
	Timetable timetable = null;
	TabHost tabHost=null;
	ProgressDialog progressDialog=null;
	int currentWeek = Timetable.getCurrentWeek();
	
	/**
	 * true if user is just browsing - from intent
	 */
	boolean isTemporaryTimetable = false;
	
	TimetableApp application;
	
	Thread onlineUpdate=null;
	
	final private Handler uiHandler = new Handler();
	
	void toast(final CharSequence message)
	{
		uiHandler.post(new Runnable()
		{			
			@Override
			public void run()
			{
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();				
			}
		});
	}
	
	private Thread downloadPdf = null;
	
	private int lastUpdatedHour = 0; // screen should refresh every hour
	private Timer timedUpdateTimer = null;
       
    void downloadTimetable()
    {
    	try
    	{
    		showProgressPopup("Downloading timetable...");
    		onlineUpdate = 	new Thread() { 
    	        public void run() 
    	        { 
    	        	timetable.downloadFromWebsiteEx(getApplicationContext());
    	        } 
    	        
    		};
    		onlineUpdate.start();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		dismissProgresPopup();
    		showPopupMessage("Error: Downloading thread could not be started.");
    	}
    }
    
    void loadTimetable()
    {
    	showProgressPopup("Loading...");
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				timetable.importSavedTimetable(getApplicationContext());
			}
		}).start();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    }
    
    View createTabView(final Context context, final CharSequence text)
    {
    	View view = LayoutInflater.from(context).inflate(R.layout.tab_view, null);
    	TextView tv = (TextView) view.findViewById(R.id.tabText);
    	tv.setText(text);
    	
    	return view;
    }
    
    void setupTabs(final TabHost tabHost)
    {
		tabHost.setup();
		
		for (int i=0; i < Timetable.dayNames.length-2; i++)
		{			
			TabSpec tabSpec = tabHost.newTabSpec(Timetable.dayNames[i]);
			tabSpec.setContent(R.id.Day);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) 
			{
				View tabView = createTabView(tabHost.getContext(), Timetable.dayNames[i].substring(0, 3));
				tabSpec.setIndicator(tabView);
			}
			else tabSpec.setIndicator(Timetable.dayNames[i].substring(0, 3));
			
			tabHost.addTab(tabSpec);
		}
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener()
		{
			
			@Override
			public void onTabChanged(String arg0)
			{
				updateTabContent(tabHost, true);	
			}
			
		});
		
		tabHost.setCurrentTab(1);				
		tabHost.setCurrentTab(getDay(true));
//		updateTabContent(tabHost);
    }
    
    /**
     * Returns tab id corresponding to current day
     * @param defaultMonday	true if value should default to monday
     * @return day id
     */
    int getDay(boolean defaultMonday)
    {
    	switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
    	{
    		case Calendar.MONDAY:
    			return 0;
    		case Calendar.TUESDAY:
    			return 1;
    		case Calendar.WEDNESDAY:
    			return 2;
    		case Calendar.THURSDAY:
    			return 3;
    		case Calendar.FRIDAY:
    			return 4;
			default: //shows monday if its the weekend
				if (defaultMonday) return 0;
				else return -1;
    	}
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
				if (weekRange == Timetable.INVALID_WEEK_RANGE) timetable = new Timetable(course, year, weeks, this, this, application.getSettings());
				else timetable = new Timetable(course, year, weekRange, this, this, application.getSettings());
				
//				timetable.loadGroups(getApplicationContext());
				
				loadTimetable();
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
		application = (TimetableApp) getApplication();
		processIntent();
				
		if (timetable == null)
		{
			timetable = new Timetable("", 1, Timetable.SEMESTER_1, this, this, application.getSettings());
			
/*			// leave this here until update
			timetable.loadGroups(getApplicationContext());*/
			
			if (timetable.loadSettings(getApplicationContext()))
			{
				loadTimetable();
			}
		}	
		
		tabHost = (TabHost) findViewById(R.id.tabHost);
		
		setupTabs(tabHost);
	}
	/*
	 * Schedules updated for the next full hour
	 */
	private void scheduleTabUpdate()
	{
		timedUpdateTimer = new Timer();
//		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		
		Calendar nextUpdate = Calendar.getInstance();
		nextUpdate.set(Calendar.MINUTE, 0);
		nextUpdate.set(Calendar.SECOND, 0);
		nextUpdate.add(Calendar.HOUR, 1);
//		nextUpdate.roll(GregorianCalendar.HOUR_OF_DAY, true);
		
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
						updateTabContent(tabHost, false);
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
		
		scheduleTabUpdate();
		if (lastUpdatedHour !=  Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) updateTabContent(tabHost, false);
	};
	
	@Override
	protected void onPause()
	{
		super.onPause();
		timedUpdateTimer.cancel();
	};
	

	/**
	 * Adds space between elements
	 * @param inflater
	 * @param num num of hours
	 * @param highligh id of element to be highlighted
	 * @return
	 */
	View getSpacer(LayoutInflater inflater, int num, Animation animation, int highligh)
	{
		ViewGroup space = (ViewGroup) inflater.inflate(R.layout.timetable_event_empty, null);
		ViewGroup container = (ViewGroup) space.findViewById(R.id.separator_dot_container);
		for (int i=0; i < num; i++)
		{
			ImageView imageView = new ImageView(this);
			imageView.setImageResource((i == highligh)?R.drawable.separator_dot_selected:R.drawable.separator_dot);
			imageView.setPadding(5,5,5,5);
			
			container.addView(imageView);
		}
		if (animation != null) space.setAnimation(animation);
		return space;
	}
	
	private Animation makeTileAnimation(int n)
	{
		Animation tileAnimation = 
				new TranslateAnimation(0,0,1000,0);
		tileAnimation.setDuration(250);
		tileAnimation.setStartOffset(n*30);
		tileAnimation.setFillAfter(true);
		
		return tileAnimation;
	}
	
	void drawTimetable(ViewGroup parentLayout, boolean animate)
	{
		setTitle(timetable.getCourseYearCode());
		LayoutInflater inflater = getLayoutInflater();
		int currentTab = tabHost.getCurrentTab();
		
		int hour=0;
		boolean isToday = (getDay(false) == currentTab);
		if (isToday)
		{
			hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		}
		
		TimetableDay day = timetable.getDayTimetable(currentTab);
		
		ViewGroup hsViews[] = new ViewGroup[] {
			null, null, null, null, null, null, 
			null, null, null, null, null, null,
			null, null, null, null, null, null, 
			null, null, null, null, null, null};
		
		int n=0,
				lastEndHour=0;
		TimetableEvent selectedEvent=null;
//		int width = parentLayout.getWidth();

		int showWeek = application.getSettings().getOnlyCurrentWeek()?currentWeek : 0;
		for (TimetableEvent event : day.getClasses()) if (event.isGroup(application.getSettings().getHiddenGroups()) && event.isInWeek(application.getSettings().getOnlyCurrentWeek()?this.currentWeek:0))
		{
//			if (application.getSettings().getOnlyCurrentWeek() == true && event.isInWeek(this.currentWeek) == false) continue;
			
			int numClassesAtCurrentHour = day.getNumClasses(event.getStartHour(), application.getSettings().getHiddenGroups(), showWeek);
			boolean isSingleEvent = numClassesAtCurrentHour == 1;
			View tile = event.getTile(this, isSingleEvent == false); 
			
			// mark current event
			if ((isToday && selectedEvent == null && event.getEndHour() > hour)
					|| (selectedEvent != null && (event.getStartHour() == selectedEvent.getStartHour())))
			{
				selectedEvent = event;
				int rDrawable = event.isEventOn(hour)?R.drawable.selected_item_selector:R.drawable.upcoming_item_selector;
				
				if (isSingleEvent)
					((RelativeLayout) tile.findViewById(R.id.timetable_event_small)).setBackgroundResource(rDrawable);
				else
					((LinearLayout) tile.findViewById(R.id.timetable_event_tiny)).setBackgroundResource(rDrawable);
				
			}
			
			Animation tileAnimation = makeTileAnimation(n);
				
			
			if (lastEndHour > 0)
			{
				int hours = event.getStartHour()-lastEndHour;
				if (hours > 0) 
				{
					parentLayout.addView(getSpacer(inflater, hours,animate?tileAnimation:null, isToday?(hour-lastEndHour):(-1)));
					if (animate) n++;
				}
			}
			lastEndHour=event.getEndHour();
			
			/*Add to layout*/
			if (isSingleEvent) 
			{
				if (animate) tile.setAnimation(tileAnimation);
				parentLayout.addView(tile);
			}
			else
			{
//				tile.setMinimumWidth(width / numClassesAtCurrentHour);
				int startTime = event.getStartHour();
				ViewGroup sameHourEventContainer = hsViews[startTime];
				
				// create scroller if doesnt exist
				if (sameHourEventContainer == null)
				{						
					sameHourEventContainer = new HorizontalScrollView(this);
					((HorizontalScrollView) sameHourEventContainer).setFillViewport(true);
					
					LayoutParams sameHourContainerParams = new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					sameHourEventContainer.setLayoutParams(sameHourContainerParams);

					if (animate) sameHourEventContainer.setAnimation(tileAnimation);
						ViewGroup host = new LinearLayout(this);
						host.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
									
					sameHourEventContainer.addView(host);
//					sameHourEventContainer.setScrollbarFadingEnabled(false);
					
					hsViews[startTime] = sameHourEventContainer;
					parentLayout.addView(sameHourEventContainer);
				}
				else n--;
				
				// casting container's inner layout
				((ViewGroup) sameHourEventContainer.getChildAt(0)).addView(tile);
			}
			n++;
		}
	}
	
	
	void updateTabContent(TabHost tabHost, boolean animate)
	{
		lastUpdatedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		LinearLayout layout  = (LinearLayout) findViewById(R.id.dayContent);
		if (timetable.isContentReady() == true)
		{
			layout.removeAllViews();			
			drawTimetable(layout, animate);
			
		}
		else
		{
			layout.removeAllViews();
		}	
		Log.i(logTag, "Tab content updated.");
	}
	
	/*@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			if (lastUpdatedHour != Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) updateTabContent(tabHost, false);
		}
	}*/
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case SETTINGS_REQUEST_CODE:
			if (resultCode == RESULT_OK)
			{
				timetable.loadSettings(getApplicationContext());
				loadTimetable();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	void downloadPdf()
	{
		if (downloadPdf==null ||  downloadPdf.isAlive() == false)
		{
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("Download PDF")
			.setMessage(String.format(Locale.getDefault(), "This will download timetable in PDF format to your %s folder.", Connection.downloadsFolder))
			.setPositiveButton("Continue", new OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					try
					{						
//						Toast.makeText(getApplicationContext(), "Downloading PDF...", Toast.LENGTH_LONG).show();
						showProgressPopup("Preparing download...");
						downloadPdf = new Thread()
						{
							public void run()
							{
								Log.d(logTag, "PDF download thread started.");
								timetable.downloadPdf(getApplicationContext());
							}
						};
						downloadPdf.start();
					}
					catch (Exception e)
					{
						e.printStackTrace();
						TimetableActivity.this.onDownloadPdfStarted(false);
					}
				}
			})
			.setNegativeButton("Cancel", null)
			.show();
			
		}
	}
	
	void showSettingsScreen()
	{
		Intent settingsScreen = new Intent(TimetableActivity.this, SettingsActivity.class);
		startActivityForResult(settingsScreen, SETTINGS_REQUEST_CODE);
	}
	
	void showGroupSelectionDialog()
	{
		ArrayList<String> gList = timetable.getGroupsInTimetable();
		
		if (gList.isEmpty())
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
//					downloadTimetable();
					onBtnRefreshPressed();
				}
			})
			.setIcon(R.drawable.ic_launcher)
			.show();
		}
		else
		{
			String[] gArray = new String[gList.size()];
			gList.toArray(gArray);
			
			gList = (ArrayList<String>) application.getSettings().getHiddenGroups();
			String[] hgArray = new String[gList.size()];
			gList.toArray(hgArray);
			
			GroupSelectionDialog dialog = new GroupSelectionDialog();
			dialog.setGroups(gArray, hgArray);
			dialog.show(getSupportFragmentManager(), "GroupSelector");
		}
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId,
			com.actionbarsherlock.view.MenuItem item)
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
			downloadPdf();
			return true;
			
		case R.id.menu_refresh_timetable:
			onBtnRefreshPressed();	
			return true;
			
		case R.id.menu_settings:
			showSettingsScreen();
			return true;
			
		case R.id.menu_groups:
			showGroupSelectionDialog();
			return true;
			
		case R.id.share_timetable:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, timetable.toString());
			startActivity(Intent.createChooser(intent, "Share timetable via..."));
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
//				.setView(LayoutInflater.from(this).inflate(R.layout.timetable_event_small, null))
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
//						intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.mick88.dittimetable"));
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.mick88.dittimetable");
						intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!");
						startActivity(Intent.createChooser(intent, "Share app"));
						
					}
				})
				.create();
			dialog.show();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	void onBtnRefreshPressed()
	{
		downloadTimetable();	
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.activity_timetable, menu);
		return true;
	}
	
	public void reportProgress(final int progress, final int max)
	{
		if (progressDialog != null && progressDialog.getMax() >= progress)
		{
			uiHandler.postDelayed(new Runnable()
			{
				
				@Override
				public void run()
				{
					if (progressDialog != null)
						{
							progressDialog.setIndeterminate(false);
							progressDialog.setMax(max);
							progressDialog.setProgress(progress);
						}
				}
			}, 1);
			
		}
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
//		progressDialog.setOnDismissListener(listener);
		
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
	public void onTimetableLoadFinish(final ErrorCode errorCode)
	{		
		Log.d(logTag, "Timetable finished with code: "+errorCode.toString());
		uiHandler.postDelayed(new Runnable()
		{			
			@Override
			public void run()
			{
				dismissProgresPopup();
				switch (errorCode)
				{
					case noError:
						updateTabContent(tabHost, true);
						break;
					case connectionFailed:
						showPopupMessage("Server didn't respond");
						break;
					case wrongDataReceived:
						showPopupMessage("Cannot download timetable. Web timetables may be down. Please try again later.");
						break;
					case timetableIsEmpty:
						showPopupMessage("There are no events in selected timetable");
						break;
					case wrongLoginDetails:
						showPopupMessage("Your login details are incorrect. Please attempt login again.");
						break;
					case noLocalCopy:
						downloadTimetable();
						break;
					case wrongCourse:
						showPopupMessage("Selected course does not exist. Please review your settings.");
						break;
					case serverLoading:
						showPopupMessage("The server is loading commonly used data at the moment");
						break;
					case usernamePasswordNotPresent:
						toast("Please provide username and password");
						break;
					case sessionExpired:
						showPopupMessage("Session expired. Please try again.");
						break;
					case noEvents:
						toast("There are no events in selected timetable.");
						break;
					default:
						showPopupMessage("Unknown Error");
						break;
				}
				
			}
		}, 10);
	}

	@Override
	public void onDebugStringReceived(final String s)
	{
		uiHandler.post(new Runnable()
		{			
			@Override
			public void run()
			{
				showPopupMessage(s);				
			}
		});
		
	}

	@Override
	public void onDownloadPdfStarted(boolean success)
	{
		dismissProgresPopup();
		
		if (success == false)
		{
			toast("Download failed");
		}
	}

	@Override
	public void onSettingsNotComplete()
	{
		toast("Please fill the settings before using the app.");
		showSettingsScreen();	
	}

	@Override
	public void onGroupsSelected(
			ArrayList<String> selected,
			ArrayList<String> unselected)
	{
		for (String s : selected) application.getSettings().unhideGroup(s);
		for (String s : unselected) application.getSettings().hideGroup(s);
		application.getSettings().saveSettings();
		loadTimetable();		
	}
	
	

}
