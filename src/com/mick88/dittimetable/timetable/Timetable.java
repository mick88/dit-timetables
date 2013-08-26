package com.mick88.dittimetable.timetable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.Connection;
import com.mick88.dittimetable.screens.TimetableActivity;

/**
 * Class containing a timetable divided into days
 * @author Michal
 *
 */
public class Timetable 
{
	public enum ErrorCode
	{
		noError,
		noLocalCopy,
		connectionFailed,
		wrongDataReceived,
		wrongLoginDetails,
		timetableIsEmpty,
		wrongCourse,
		serverLoading,
		usernamePasswordNotPresent,
		sessionExpired,
		noEvents,
	}
	
	public interface ResultHandler
	{
		public void onTimetableLoadFinish(ErrorCode errorCode);
		public void onDebugStringReceived(String s);
		public void onDownloadPdfStarted(boolean success);
		public void onSettingsNotComplete();
		/**
		 * Called after full data download
		 */
		public void onFullDataDownloaded();
	}
	/**
	 * All groups in currently loaded timetable
	 */
	ArrayList<String> groupsInTimetable=new ArrayList<String>();
	ResultHandler resultHandler=null;
	Activity parentActivity=null;
	final AppSettings settings;
	boolean disposed=false;
	
	String sourceString = "";
	public static final String [] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	static final String tableStart = "<table width=\"100%\" class=\"gridTable\" summary=\"Timetable Grid View\" >",
			tableGraphicStart = "id=\"scrollContent\"", // this is how I know the page shows timetable 
			tableEnd = "</table>",
			tableGraphicEnd = tableEnd,
			rowStart = "<tr>",
			rowEnd = "</tr>";
	static final int startMonth = Calendar.AUGUST,
			startDay = 27;
	
	static final String settingsSplitter = "\n",
			groupSplitter = ",";
	public static final int SETTINGS_ID_COURSE = 0,
			SETTINGS_ID_YEAR=1,
			SETTINGS_ID_WEEKS=2,
			SETTING_ID_USERNAME=3,
			SETTING_ID_PASSWORD=4;
	
	Boolean valid=true; //changed to false if error is detected
	Connection connection = null;
	
	final String logTag = "Timetable";
	
	public static final String 
			SEMESTER_1 = "4-20",
			SEMESTER_2  = "23-30,33-37",
			ALL_WEEKS  = "1-52",
			SETTINGS_FILE_NAME = "settings.dat",
			GROUPS_FILE_NAME = "groups.dat";
	public static final int INVALID_WEEK_RANGE = -1;
	
	Date lastUpdated = null;
	
	public String getCourse()
	{
		return course;
	}
	
	public CharSequence describe()
	{
		return new StringBuilder(course).append('-').append(year);
	}
	
	public CharSequence describeWeeks()
	{
		if (weeks.equals(SEMESTER_1)) return "Semester 1";
		else if (weeks.equals(SEMESTER_2)) return "Semester 2";
		return new StringBuilder("Weeks ").append(weeks);
	}
	
	public Date getLastUpdated()
	{
		return lastUpdated;
	}
	
/*	@Deprecated
	public ArrayList<String> getHiddenGroups()
	{
		return (ArrayList<String>) settings.getHiddenGroups();
	}*/
	
	public ArrayList<String> getGroupsInTimetable()
	{
		return groupsInTimetable;
	}
	
	public void hideGroup(String groupCode)
	{
		if (groupCode.equals(getCourseYearCode())) return; //cannot hide whole class
		settings.hideGroup(groupCode);
	}
	
	public Connection getConnection()
	{
		return connection;
	}
	
	/*Query data*/
	private String course = "DT211";
	private int year=2;
	private String weeks = SEMESTER_1;
	private int weekRange = INVALID_WEEK_RANGE; // alternative to weeks
	private String key = "201213";
	
	
	/**
	 * Gets current calendar semester
	 */
	public static int getSemester()
	{
		Calendar c = Calendar.getInstance();
		int month = c.get(Calendar.MONTH),
				day = c.get(Calendar.DAY_OF_MONTH);
		if ((month > startMonth) || (month >= startMonth) && (day >= startDay))
			return 1;
		else
			return 2;
	}
	
	public static String getDataset()
	{
		Calendar c = Calendar.getInstance();
		int startYear=0;
		
		if (getSemester() == 1)
			startYear = c.get(Calendar.YEAR);
		else
			startYear = c.get(Calendar.YEAR)-1;
		
		StringBuilder builder = new StringBuilder(6);
		builder.append(startYear);
		builder.append((startYear+1) % 100);
		return builder.toString();
	}
	
	public static int getCurrentWeek()
	{
		Calendar cal = Calendar.getInstance();
		int startYear = (getSemester() == 1) ? cal.get(Calendar.YEAR) : (cal.get(Calendar.YEAR)-1);
		
		Date yearStart = new GregorianCalendar(startYear, startMonth, startDay).getTime();
		Date today = new GregorianCalendar().getTime();
		
		long milisBetween = today.getTime() - yearStart.getTime();
		int daysBetween = (int) (milisBetween / 1000 / 60 / 60 / 24);
		
		return (daysBetween / 7)+1;	
	}
	
	public static final int dayMonday = 0,
			dayTueday = 1,
			dayWednesday = 2,
			dayThursday = 3,
			dayFriday = 4,
			daySaturday = 5;	
	static final int numDays = daySaturday+1;
	
	public boolean isContentReady()
	{
		return (valid);
	}	
	
	public Map<String,String> ToHashMap()
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("Course", this.course);
		result.put("Year", Integer.toString(this.year));
		result.put("Week range", this.weeks);
		
		StringBuilder groups = new StringBuilder();
		for (String group : groupsInTimetable)
		{
			if (settings.getHiddenGroups().contains(group) == false) groups.append(group).append(',');
		}
		result.put("groups", groups.toString());
		
		result.put("username", settings.getUsername());
		result.put("password", settings.getPassword());
		
		return result;
	}
	
	public static int getDayByName(String name)
	{
		for (int i=0; i < dayNames.length; i++)
		{
			if (dayNames[i].contains(name)) return i;
		}
		return -1;
	}
	
	public TimetableDay getDay(int id)
	{
		return days[id];
	}
	
	public String getCourseYearCode()
	{
		return String.format(Locale.getDefault(), "%s/%d", course, year);
	}
	
	private TimetableDay[] days = new TimetableDay[numDays];
	
	public Timetable(ResultHandler resultHandler, Activity parentActivity, AppSettings settings)
	{
		/*Initialize days*/
		this.days[dayMonday] = new TimetableDay(dayNames[dayMonday], this);
		this.days[dayTueday] = new TimetableDay(dayNames[dayTueday], this);
		this.days[dayWednesday] = new TimetableDay(dayNames[dayWednesday], this);
		this.days[dayThursday] = new TimetableDay(dayNames[dayThursday], this);
		this.days[dayFriday] = new TimetableDay(dayNames[dayFriday], this);
		this.days[daySaturday] = new TimetableDay(dayNames[daySaturday], this);
		
		this.connection = new Connection(settings);
		this.key = getDataset();
		
		this.settings = settings;
		this.resultHandler = resultHandler;
		this.parentActivity = parentActivity;
		
		this.course = settings.getCourse();
		this.year = settings.getYear();
		this.weeks = settings.getWeeks();			
	}
	
    public static int getTodayId(boolean defaultMonday)
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
	
	public TimetableDay getToday(boolean defaultMonday)
	{
		int id = getTodayId(defaultMonday);
		if (id == -1 | id >= days.length) return null;
		return days[id];
	}
	
	public Timetable(String course, int year, String weeks, ResultHandler resultHandler, Activity parentActivity, AppSettings settings)
	{
		this(resultHandler, parentActivity, settings);
		
		this.course = course;
		this.year = year;
		this.weeks = weeks;		
	}
	
	public Timetable(String course, int year, int weekRange, ResultHandler resultHandler, Activity parentActivity, AppSettings settings)
	{
		this(resultHandler, parentActivity, settings);
		
		this.course = course;
		this.year = year;
		this.weekRange = weekRange;
	}
	
	/** 
	 * Gets the timetable url
	 * @return address to the web timetable
	 */
	public String getQueryAddress()
	{
		if (weekRange == -1) return String.format(Locale.ENGLISH, 
				"?reqtype=timetable&action=timetable&sKey=%s%%7C%s&sTitle=Computing&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&sWeeks=%s&sType=course&instCode=-2&instName=", 
				key, course, year, weeks);
		else return String.format(Locale.ENGLISH, 
				"?reqtype=timetable&action=timetable&sKey=%s%%7C%s&sTitle=Computing&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&weekRange=%d&sType=course&instCode=-2&instName=", 
				key, course, year, weekRange);
	}

	/**
	 * Downloads timetable content from web timetables - graphic view
	 * @param context
	 * @return true if successful
	 */
	public boolean downloadFromWebsite(Context context)
	{
		if (connection.areCredentialsPresent() == false)
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.usernamePasswordNotPresent);
			return false;
		}

		String query = getQueryAddress();
		
		String string = connection.getContent(query);
		
		if (string == null)
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.connectionFailed);
			return false;
		}	
		
		
		if (string.contains("You must login again."))
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.sessionExpired);
			return false;
		}
		
		if (string.contains("There are no events to display."))
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.noEvents);
			return false;
		}
		
		if (string.contains("The system is loading commonly used data at the moment"))
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.serverLoading);
			return false;
		}
		
		if (string.contains("Your login details are incorrect. Please attempt login again."))
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.wrongLoginDetails); 
			return false;
		}
		
		if (string.contains("There are no events in the selected timetable."))
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.noEvents); 
			return false;
		}
		if (string.contains("There are no course records matching the criteria."))
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.wrongCourse); 
			return false;
		}
		
		if (string.contains(tableGraphicStart) == false)
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.wrongDataReceived);
			Log.d(logTag, string);
			return false;
		}
		
		boolean result = parseGraphicStringEx(string, false);
		
		if (result == true) 
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.noError);
		}
		else 
		{
			if (resultHandler != null) resultHandler.onTimetableLoadFinish(ErrorCode.wrongDataReceived);
		}
		
		// finalize by saving to file and downloading detailde info
		if (result == true) 
		{
			for (TimetableDay day : days)
			{
				if (disposed) break;
				day.downloadAccitionalInfo(context);
			}
			
			if (resultHandler != null) resultHandler.onFullDataDownloaded();
			exportTimetable(context);
			Log.i(logTag, "Timetable successfully downloaded");
			lastUpdated = new Date();
		}

		return result;
	}
	
	void addClassGroup(String groupCode)
	{
		if (groupsInTimetable.contains(groupCode) == false)
		{
			groupsInTimetable.add(groupCode);
			Log.i(logTag, "Added to group list: "+groupCode);
		}
	}
	
	void clearEvents()
	{
		valid=false;
		for (int i=0; i < numDays; i++)
		{
			days[i].clearEvents();
		}
		groupsInTimetable.clear();
	}
	
	List<TimetableDay> getDayOrder(Element table)
	{
		ArrayList<TimetableDay> result = new ArrayList<TimetableDay>();
		if (table != null)
		{
			Elements days = table.select("div");
			for (Element day : days)
			{
				String dayStr = day.text().trim();
				for (TimetableDay d : this.days)
				{
					if (d.name.contains(dayStr))
					{
						result.add(d);
						break;
					}
				}
			}
		}
		return result;
	}
	
	public AppSettings getSettings()
	{
		return settings;
	}
	
	private void reportProgress(int progress, int max)
	{
		if (parentActivity != null) ((TimetableActivity)parentActivity).reportProgress(progress, Math.max(max, progress));
	}
	
	/**
	 * Parses the new timetables
	 * @param string html
	 */
	public boolean parseGraphicStringEx(String string, boolean allowCache)
	{		
		Document doc = Jsoup.parse(string);
		
		Elements topLevel = doc.select("div.highAndWide");
		Element parentContainer = topLevel.first();
		
		List<TimetableDay> days = getDayOrder(doc.select("div#scrollDay").first());
		
		if (parentContainer == null || days.isEmpty()) return false;
		
		Elements entries = parentContainer.select("div");
		int max = entries.size()-6, nEvents=0;
		reportProgress(nEvents, max);
		
		clearEvents();
		TimetableDay currentDay=null;
		
		for (Element entry : entries)
		{
			if (disposed) return false;
			String id = entry.id();
			if (id.equalsIgnoreCase("r0")) 
			{
				currentDay = days.get(dayMonday);
			}
			else if (id.equalsIgnoreCase("r1")) 
				{
					currentDay = days.get(dayTueday);
				}
			else if (id.equalsIgnoreCase("r2")) 
				{
					currentDay = days.get(dayWednesday);
				}
			else if (id.equalsIgnoreCase("r3")) 
				{
					currentDay = days.get(dayThursday);
				}
			else if (id.equalsIgnoreCase("r4")) 
				{
					currentDay = days.get(dayFriday);
				}
			else if (id.equalsIgnoreCase("r5")) 
				{
					currentDay = days.get(daySaturday);
				}
			else if (currentDay != null)
			{
				nEvents += currentDay.parseHtmlEvent(entry, parentActivity.getApplicationContext(), allowCache);
				reportProgress(nEvents, max);
			}
			else
			{
				Log.w(logTag, "Event cannot be parsed: "+entry.toString());
			}
		}

		valid=(nEvents > 0);
		
		for (TimetableDay day : days)
		{
			day.sortEvents();
		}
		sortGroups();
		return valid;
	}
	
	public int getNumDays()
	{
		return days.length;
	}
	
	public Activity getParentActivity()
	{
		return parentActivity;
	}
	
	void sortGroups()
	{
		Collections.sort(groupsInTimetable);
	}
	
	String getFilename()
	{
		return String.format(Locale.getDefault(), "%s_%d_%s_%s.txt", course, year, weeks, key);
	}
	
	@Deprecated
	public void saveToFile(Context context, String content) throws IOException
	{
		String filename = getFilename();
		FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);			
		byte[] buffer = content.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
		Log.i(logTag, "Timetable saved to "+filename);
	}
	
	public void downloadPdf(Context context)
	{
		String weeksStr;
		if (weeks.equals(SEMESTER_1)) weeksStr = "S1";
		else if (weeks.equals(SEMESTER_2)) weeksStr = "S2";
		else weeksStr = "W"+weeks;
		String filename = String.format(Locale.getDefault(), "Timetable_%s-%d_%s.pdf", course, year, weeksStr);
		
		Log.d(logTag, "Downloading PDF file: "+filename);
		/*String query = String.format(Locale.getDefault(), 
				"?reqtype=timetablepdf&sKey=%s%%7C%s%%7C%s%%252F%d%%7C%d&sTitle=DIT&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&sWeeks=%s&sType=class&instCode=-2&instName=",
				key, 
				course, 
				course, year, 
				year, 
				year, weeks);*/
		String query = String.format(Locale.getDefault(), 
				"?reqtype=timetablepdf&sKey=%s%%7C%s&sTitle=DIT&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&sWeeks=%s&sType=course&instCode=-2&instName=",
				key, course, year, weeks);
		Log.d(logTag, "Getting content with file address:");
		
		String string = connection.getContent(query);
		if (string == null) 
		{
			resultHandler.onDownloadPdfStarted(false);
			return;
		}
		
		//Log.v(logTag, string);
		Elements elements = Jsoup.parse(string).select("a[href$=.pdf]");
		Element pdfLink = elements.first();
		
		if (pdfLink == null)
		{
			resultHandler.onDownloadPdfStarted(false);
			return;
		}
		
		String address = Connection.ROOT_ADDRESS_PDF + pdfLink.attr("href");
		
		try
		{
			connection.downloadPdf(context, address, filename);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			resultHandler.onDownloadPdfStarted(false);
			return;
		}
		Log.i(logTag, "PDF download started. Filename: "+filename);
		resultHandler.onDownloadPdfStarted(true);
		return;
	}
	
	/*@Deprecated
	public void loadFromFile(Context context)
	{
		String filename = getFilename();			
		StringBuffer sb = new StringBuffer();
		try
		{
			if (context.getFileStreamPath(filename).exists() == false)
			{
				Log.i(logTag, "Timetable could not be loaded from file. File does not exist "+filename);
				resultHandler.onTimetableLoadFinish(ErrorCode.noLocalCopy);
				return;
			}
			
			final int BUFFER_SIZE = 30000;
			FileInputStream f = context.openFileInput(filename);
			
			byte[] buffer = new byte[BUFFER_SIZE];				
			
			
			while (f.read(buffer) > 0)
			{
				String line = new String(buffer);
				sb.append(line);
				
				buffer = new byte[BUFFER_SIZE];
			}
			
			f.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			resultHandler.onTimetableLoadFinish(ErrorCode.noLocalCopy);
		}
		
		Log.i(logTag, "Timetable loaded from "+filename);
		
		if (parseGraphicStringEx(sb.toString(), true))
		{
			Log.i(logTag, "Timetable successfully loaded from file");
			
			File file = context.getFileStreamPath(filename);
			lastUpdated = new Date(file.lastModified());
			resultHandler.onTimetableLoadFinish(ErrorCode.noError);
		}
		else resultHandler.onTimetableLoadFinish(ErrorCode.noLocalCopy);
		return;
	}*/
	
	/*@Deprecated
	public boolean loadSettings(Context context)
	{
		String [] settings = null;
		try
		{
			settings = readSettings(context);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			resultHandler.onSettingsNotComplete();
			return false;
		}
		
		if (settings == null || settings.length == 0)
		{
			resultHandler.onSettingsNotComplete();
			return false;
		}
		
		course = settings[SETTINGS_ID_COURSE];
		weeks = settings[SETTINGS_ID_WEEKS];
		year = Integer.parseInt(settings[SETTINGS_ID_YEAR]);
		
		try
		{
			// move settings to the new format
			if (TextUtils.isEmpty(this.settings.getUsername())) this.settings.setUsername(settings[SETTING_ID_USERNAME]);
			if (TextUtils.isEmpty(this.settings.getPassword())) this.settings.setPassword(settings[SETTING_ID_PASSWORD]);
			
			if (TextUtils.isEmpty(this.settings.getCourse())) this.settings.setCourse(settings[SETTINGS_ID_COURSE]);
			if (TextUtils.isEmpty(this.settings.getWeeks())) this.settings.setWeeks(settings[SETTINGS_ID_WEEKS]);
			if (this.settings.getYear() == 0) this.settings.setYear(Integer.valueOf(settings[SETTINGS_ID_YEAR]));
			this.settings.saveSettings();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			resultHandler.onSettingsNotComplete();
		}
		clearEvents();
		Log.i(logTag, "settings loaded");
		
		
		
		return true;
	}*/
	
	public static void writeSettings(Context context, String[] settingString) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		for (String s : settingString) 
		{
			builder.append(s);
			builder.append(settingsSplitter);
		}
		FileOutputStream file = context.openFileOutput(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
		
		byte[] buffer = builder.toString().getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}
	
	@Deprecated
	public void saveGroups(Context context) throws IOException
	{
		settings.saveSettings();
	}
	
	public static String [] readSettings(Context context) throws IOException
	{
		final int BUFFER_SIZE = 500;
		FileInputStream f = context.openFileInput(SETTINGS_FILE_NAME);
		
		byte[] buffer = new byte[BUFFER_SIZE];				
		
		StringBuffer sb = new StringBuffer();
		while (f.read(buffer) > 0)
		{
			String line = new String(buffer);
			sb.append(line);
			
			buffer = new byte[BUFFER_SIZE];
		}
		
		f.close();
		return sb.toString().split(settingsSplitter);
	}
	
	/*
	 * Saves timetable locally
	 */
	final String daySeparator = ":day:";
	
	public void exportTimetable(Context context)
	{
		StringBuilder builder = new StringBuilder();
		for (TimetableDay day : days)
		{
			builder.append(day.export());
			builder.append(daySeparator);
		}
		try
		{
			writeFile(context, "export"+getFilename(), builder.toString());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void importSavedTimetable(Context context)
	{
		if (importTimetable(context)) 
		{
			valid=true;
			resultHandler.onTimetableLoadFinish(ErrorCode.noError);
		}
		else resultHandler.onTimetableLoadFinish(ErrorCode.noLocalCopy);
	}
	
	private boolean importTimetable(Context context)
	{
		String content=readFile(context, "export"+getFilename());
		if (content == null) return false;
		
		int dayId = dayMonday,
				n=0;
		String [] day = content.split(daySeparator);
		if (day.length < daySaturday) return false;
		clearEvents();
		
		for (String d : day)
		{
			if (dayId < days.length)
			{
				n += days[dayId++].importFromString(d);
			}
		}
		return (n > 0);
	}
	
	public TimetableDay getDayTimetable(int day)
	{
		return days[day];
	}
		
	public String readFile(Context context, String filename)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			if (context.getFileStreamPath(filename).exists() == false)
			{
				return null;
			}
			
			final int BUFFER_SIZE = 30000;
			FileInputStream f = context.openFileInput(filename);
			
			byte[] buffer = new byte[BUFFER_SIZE];	
			
			while (f.read(buffer) > 0)
			{
				String line = new String(buffer);
				sb.append(line);				
				buffer = new byte[BUFFER_SIZE];
			}
			
			f.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder= new StringBuilder();
		int n=0,
			week = getCurrentWeek();
		for (TimetableDay day : days)
		{
			if (day.getClasses().isEmpty()) continue;
			String s = day.toString(settings.getHiddenGroups(), settings.getOnlyCurrentWeek()?week:0);
			if (TextUtils.isEmpty(s)) continue;
			
			if (n > 0) builder.append("\n\n");
			builder.append(s);			
			n++;
		}
		return builder.toString();
	}
	
	public void writeFile(Context context, String filename, String content) throws IOException
	{
		FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);			
		byte[] buffer = content.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}
	
	public boolean isDisposed()
	{
		return disposed;
	}
	
	/**
	 * Cancel current tasks and remove listeners.
	 */
	public void dispose()
	{
		resultHandler = null;
		parentActivity = null;
		disposed=true;
		Log.d("Timetable", "Timetable disposed "+describe());
	}
	
}
