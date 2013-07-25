package com.mick88.dittimetable.timetable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.screens.TimetableActivity;
import com.mick88.dittimetable.utils.FileUtils;
import com.mick88.dittimetable.web.Connection;

/**
 * Class containing a timetable divided into days
 * @author Michal
 *
 */
public class Timetable 
{
	public enum ErrorCode
	{
		connectionFailed,
		noError,
		noEvents,
		noLocalCopy,
		serverLoading,
		sessionExpired,
		timetableIsEmpty,
		usernamePasswordNotPresent,
		wrongCourse,
		wrongDataReceived,
		wrongLoginDetails,
	}
	
	public interface ResultHandler
	{
		public void onDebugStringReceived(String s);
		public void onDownloadPdfStarted(boolean success);
		/**
		 * Called after full data download
		 */
		public void onFullDataDownloaded();
		public void onSettingsNotComplete();
		public void onTimetableLoadFinish(ErrorCode errorCode);
	}
	
	public static final int 
		DAY_MONDAY = 0,
		DAY_TUESDAY = 1,
		DAY_WEDNESDAY = 2,
		DAY_THURSDAY = 3,
		DAY_FRIDAY = 4,
		DAY_SATURDAY = 5,
		
		NUM_DAYS = DAY_SATURDAY+1,
		INVALID_WEEK_RANGE = -1,
		START_MONTH = Calendar.AUGUST,
		START_DAY = 27,
		
		SETTINGS_ID_COURSE = 0,
		SETTINGS_ID_YEAR=1,
		SETTINGS_ID_WEEKS=2,
		SETTING_ID_USERNAME=3,
		SETTING_ID_PASSWORD=4;
	
	public static final String [] 
			DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	public static final String 
			SEMESTER_1 = "4-20",
			SEMESTER_2  = "23-30,33-37",
			ALL_WEEKS  = "1-52",
			SETTINGS_FILE_NAME = "settings.dat",
			GROUPS_FILE_NAME = "groups.dat",
			SETTINGS_SPLITTER = "\n",
			GROUP_SPLITTER = ",",
			STR_TABLE_GRAPHIC_START = "id=\"scrollContent\""; // this is how I know the page shows timetable 
			
	public static int getCurrentWeek()
	{
		Calendar cal = Calendar.getInstance();
		int startYear = (cal.get(Calendar.MONTH) >= START_MONTH) ? cal.get(Calendar.YEAR) : (cal.get(Calendar.YEAR)-1);
		
		Date yearStart = new GregorianCalendar(startYear, START_MONTH, START_DAY).getTime();
		Date today = new GregorianCalendar().getTime();
		
		long milisBetween = today.getTime() - yearStart.getTime();
		int daysBetween = (int) (milisBetween / 1000 / 60 / 60 / 24);
		
		return (daysBetween / 7)+1;		
	}
	
	public static String getDataset()
	{
		Calendar c = Calendar.getInstance();
		int startYear=0;
		if ((c.get(Calendar.MONTH) >= START_MONTH) && (c.get(Calendar.DAY_OF_MONTH) >= START_DAY))
		{
			startYear = c.get(Calendar.YEAR);
		}
		else 
		{
			startYear = c.get(Calendar.YEAR)-1;
		}
		StringBuilder builder = new StringBuilder(6);
		builder.append(startYear);
		builder.append((startYear+1) % 100);
		return builder.toString();
	}
	
	public static int getDayByName(String name)
	{
		for (int i=0; i < DAY_NAMES.length; i++)
		{
			if (DAY_NAMES[i].contains(name)) return i;
		}
		return -1;
	}
	
	/**
	 * Gets current calendar semester
	 */
	public static int getSemester()
	{
		Calendar c = Calendar.getInstance();
		if ((c.get(Calendar.MONTH) >= START_MONTH) && (c.get(Calendar.DAY_OF_MONTH) >= START_DAY))
		{
			return 1;
		}
		else 
		{
			return 2;
		}
	}
	
	/**
	 * Gets ID of current day
	 * @param defaultMonday
	 * @return
	 */
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
	
	/**
	 * Loads settings from the settings file into the array
	 */
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
		return sb.toString().split(SETTINGS_SPLITTER);
	}
	
	/**
	 * Saves settings
	 */
	public static void writeSettings(Context context, String[] settingString) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		for (String s : settingString) 
		{
			builder.append(s);
			builder.append(SETTINGS_SPLITTER);
		}
		FileOutputStream file = context.openFileOutput(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
		
		byte[] buffer = builder.toString().getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}
	
	Connection connection = null;
	
	/*Query data*/
	private String course = "DT211";
	
	private TimetableDay[] days = new TimetableDay[NUM_DAYS];
	
	/**
	 * For saving timetable locally
	 */
	private static final String DAY_SEPARATOR = ":day:";
	
	boolean disposed=false;
	
	/**
	 * All groups in currently loaded timetable
	 */
	Set<String> groupsInTimetable=new HashSet<String>();
	
	private String key = getDataset();// "201213";	
	Date lastUpdated = null;
	final String logTag = "Timetable";
	Activity parentActivity=null;
	ResultHandler resultHandler=null;
	final AppSettings settings;	
	Boolean valid=true; //changed to false if error is detected	
	private int weekRange = INVALID_WEEK_RANGE; // alternative to weeks
		private String weeks = SEMESTER_1;	
	private int year=2;	
	
	/**
	 * Creates new Timetable object from settings
	 */
	public Timetable(ResultHandler resultHandler, Activity parentActivity, AppSettings settings)
	{
		/*Initialize days*/
		for (int i = 0; i < NUM_DAYS; i++)
			this.days[i] = new TimetableDay(DAY_NAMES[i], this);
		
		/*this.days[DAY_MONDAY] = new TimetableDay(DAY_NAMES[DAY_MONDAY], this);
		this.days[DAY_TUESDAY] = new TimetableDay(DAY_NAMES[DAY_TUESDAY], this);
		this.days[DAY_WEDNESDAY] = new TimetableDay(DAY_NAMES[DAY_WEDNESDAY], this);
		this.days[DAY_THURSDAY] = new TimetableDay(DAY_NAMES[DAY_THURSDAY], this);
		this.days[DAY_FRIDAY] = new TimetableDay(DAY_NAMES[DAY_FRIDAY], this);
		this.days[DAY_SATURDAY] = new TimetableDay(DAY_NAMES[DAY_SATURDAY], this);*/
		
		this.connection = new Connection(settings);
//		this.key = getDataset();
		
		this.settings = settings;
		this.resultHandler = resultHandler;
		this.parentActivity = parentActivity;
		
		this.course = settings.getCourse();
		this.year = settings.getYear();
		this.weeks = settings.getWeeks();			
	}
	
	/**
	 * Creates new tiemtable object with custom settings
	 */
	public Timetable(String course, int year, int weekRange, ResultHandler resultHandler, Activity parentActivity, AppSettings settings)
	{
		this(resultHandler, parentActivity, settings);
		
		this.course = course;
		this.year = year;
		this.weekRange = weekRange;
	}	
	
	public Timetable(String course, int year, String weeks, ResultHandler resultHandler, Activity parentActivity, AppSettings settings)
	{
		this(resultHandler, parentActivity, settings);
		
		this.course = course;
		this.year = year;
		this.weeks = weeks;		
	}
	
	void addClassGroup(String groupCode)
	{
		groupsInTimetable.add(groupCode);
	}
	
	void clearEvents()
	{
		valid=false;
		for (int i=0; i < NUM_DAYS; i++)
		{
			days[i].clearEvents();
		}
		groupsInTimetable.clear();
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
		
		if (string.contains(STR_TABLE_GRAPHIC_START) == false)
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
				day.downloadAdditionalInfo(context);
			}
			
			if (resultHandler != null) resultHandler.onFullDataDownloaded();
			exportTimetable(context);
			Log.i(logTag, "Timetable successfully downloaded");
			lastUpdated = new Date();
		}

		return result;
	}
	
	public void downloadPdf(Context context)
	{
		String weeksStr;
		if (weeks.equals(SEMESTER_1)) weeksStr = "S1";
		else if (weeks.equals(SEMESTER_2)) weeksStr = "S2";
		else weeksStr = "W"+weeks;
		String filename = String.format(Locale.getDefault(), "Timetable_%s-%d_%s.pdf", course, year, weeksStr);
		
		Log.d(logTag, "Downloading PDF file: "+filename);

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
	
	/**
	 * Writes timetable to file
	 */
	public void exportTimetable(Context context)
	{
		StringBuilder builder = new StringBuilder();
		for (TimetableDay day : days)
		{
			builder.append(day.export());
			builder.append(DAY_SEPARATOR);
		}
		try
		{
			writeFile(context, "export"+getFilename(), builder.toString());
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Connection getConnection()
	{
		return connection;
	}
	
	public String getCourse()
	{
		return course;
	}

	public String getCourseYearCode()
	{
		return String.format(Locale.getDefault(), "%s/%d", course, year);
	}
	
	public TimetableDay getDay(int id)
	{
		return days[id];
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
	
	public TimetableDay getDayTimetable(int day)
	{
		return days[day];
	}
	
	String getFilename()
	{
		return String.format(Locale.getDefault(), "%s_%d_%s_%s.txt", course, year, weeks, key);
	}
	
	public Set<String> getGroupsInTimetable()
	{
		return groupsInTimetable;
	}
	
	public int getNumDays()
	{
		return days.length;
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
	
	public AppSettings getSettings()
	{
		return settings;
	}
	
	/**
	 * gets id of the current day
	 * @param defaultMonday if true, returns monday for if weekend
	 */
	public TimetableDay getToday(boolean defaultMonday)
	{
		int id = getTodayId(defaultMonday);
		if (id == -1 | id >= days.length) return null;
		return days[id];
	}
	
	public void hideGroup(String groupCode)
	{
		if (groupCode.equals(getCourseYearCode())) return; //cannot hide whole class
		settings.hideGroup(groupCode);
	}
	
	/**
	 * Loads timetable from file
	 */
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
		String content = FileUtils.readFile(context, "export"+getFilename());
		if (content == null) return false;
		
		int dayId = DAY_MONDAY,
				n=0;
		String [] day = content.split(DAY_SEPARATOR);
		if (day.length < DAY_SATURDAY) return false;
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
	
	public boolean isDisposed()
	{
		return disposed;
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
				currentDay = days.get(DAY_MONDAY);
			}
			else if (id.equalsIgnoreCase("r1")) 
				{
					currentDay = days.get(DAY_TUESDAY);
				}
			else if (id.equalsIgnoreCase("r2")) 
				{
					currentDay = days.get(DAY_WEDNESDAY);
				}
			else if (id.equalsIgnoreCase("r3")) 
				{
					currentDay = days.get(DAY_THURSDAY);
				}
			else if (id.equalsIgnoreCase("r4")) 
				{
					currentDay = days.get(DAY_FRIDAY);
				}
			else if (id.equalsIgnoreCase("r5")) 
				{
					currentDay = days.get(DAY_SATURDAY);
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
	
	private void reportProgress(int progress, int max)
	{
		if (parentActivity != null) ((TimetableActivity)parentActivity).reportProgress(progress, Math.max(max, progress));
	}
	
	@Deprecated
	void sortGroups()
	{
//		Collections.sort(groupsInTimetable);
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
	
}
