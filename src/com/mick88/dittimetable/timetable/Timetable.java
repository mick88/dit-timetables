package com.mick88.dittimetable.timetable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.TextUtils;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.utils.FileUtils;
import com.mick88.dittimetable.web.Connection;

/**
 * Class containing a timetable divided into days
 * @author Michal
 *
 */
public class Timetable implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
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
		START_DAY = 26;
	
	public static final String [] 
			DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	public static final String 
			SEMESTER_1 = "4-20",
			SEMESTER_2  = "23-30,33-37",
			ALL_WEEKS  = "1-52";
			
	public static int getCurrentWeek()
	{
		Calendar cal = Calendar.getInstance();
		int startYear = (getSemester() == 1) ? cal.get(Calendar.YEAR) : (cal.get(Calendar.YEAR)-1);
		
		Date yearStart = new GregorianCalendar(startYear, START_MONTH, START_DAY).getTime();
		Date today = new GregorianCalendar().getTime();
		
		long milisBetween = today.getTime() - yearStart.getTime();
		int daysBetween = (int) (milisBetween / 1000 / 60 / 60 / 24);
		
		return (daysBetween / 7)+1;		
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
		int month = c.get(Calendar.MONTH),
			day = c.get(Calendar.DAY_OF_MONTH);
		
		if ((month > START_MONTH) || (month >= START_MONTH) && (day >= START_DAY))
			return 1;
		else
			return 2;
	}
	
	/**
	 * Gets ID of current day
	 * @param defaultMonday
	 * @return
	 */
	public static int getTodayId(boolean defaultMonday)
    {
		int day = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7;
		if (defaultMonday)
			return (day > DAY_FRIDAY) ? DAY_MONDAY : day;
		else 
			return (day > DAY_FRIDAY) ? -1 : day;
    }
	
	/**
	 * For saving timetable locally
	 */
	private static final String DAY_SEPARATOR = ":day:";
		
	Date lastUpdated = null;
	final String logTag = "Timetable";

	protected int weekRange = INVALID_WEEK_RANGE; // alternative to weeks
	protected String weeks = SEMESTER_1;
	protected String course = "DT211";
	protected int year=2;	
	protected TimetableDay[] days = new TimetableDay[NUM_DAYS];
	
	public Timetable()
	{
		for (int i = 0; i < NUM_DAYS; i++)
			this.days[i] = new TimetableDay(i);
	}
	/**
	 * Creates new Timetable object from settings
	 */
	public Timetable(AppSettings settings)
	{
		this();
		this.course = settings.getCourse();
		this.year = settings.getYear();
		this.weeks = settings.getWeeks();			
	}
	
	/**
	 * Creates new tiemtable object with custom settings
	 */
	public Timetable(String course, int year, int weekRange, AppSettings settings)
	{
		this(settings);
		
		this.course = course;
		this.year = year;
		this.weekRange = weekRange;
	}	
	
	public Timetable(String course, int year, String weeks, AppSettings settings)
	{
		this(settings);
		
		this.course = course;
		this.year = year;
		this.weeks = weeks;		
	}
	
	void clearEvents()
	{
		for (int i=0; i < NUM_DAYS; i++)
		{
			days[i].clearEvents();
		}
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
	 * Fetch page with url to the pdf and retrn url
	 * @return
	 * @throws IOException 
	 */
	public String getPdfUrl(Connection connection) throws IOException
	{
		String query = String.format(Locale.getDefault(), 
				"?reqtype=timetablepdf&sKey=%s%%7C%s&sTitle=DIT&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&sWeeks=%s&sType=course&instCode=-2&instName=",
				TimetableDownloader.getDataset(), course, year, weeks);
		String string = connection.getContent(query);
		if (TextUtils.isEmpty(string)) 
			return null;
		
		Elements elements = Jsoup.parse(string).select("a[href$=.pdf]");
		Element pdfLink = elements.first();
		
		return  Connection.ROOT_ADDRESS_PDF + pdfLink.attr("href");
	}
	
	public int getWeekRangeId()
	{
		return weekRange;
	}
	
	/**
	 * Generate filename for downloaded PDF
	 */
	public String getPdfFileName()
	{
		final String weeksStr;
		if (weeks.equals(SEMESTER_1)) weeksStr = "S1";
		else if (weeks.equals(SEMESTER_2)) weeksStr = "S2";
		else weeksStr = "W"+weeks;
		
		return String.format(Locale.getDefault(), "Timetable_%s-%d_%s.pdf", course, year, weeksStr);
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
					if (d.getName().contains(dayStr))
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
		return String.format(Locale.getDefault(), "%s_%d_%s_%s.txt", course, year, weeks, TimetableDownloader.getDataset());
	}
	
	public Set<String> getGroupsInTimetable()
	{
		Set<String> groups = new HashSet<String>();
		for (TimetableDay day : days)
			day.getGroups(groups);
		return groups;
	}
	
	public int getNumDays()
	{
		return days.length;
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
	
	public void hideGroup(String groupCode, AppSettings settings)
	{
		if (groupCode.equals(getCourseYearCode())) return; //cannot hide whole class
		settings.hideGroup(groupCode);
	}
	
	/**
	 * Loads timetable from file
	 */
	public void importSavedTimetable(Context context)
	{
		if (importTimetable(context) == false) 
			throw new Exceptions.NoLocalCopyException();
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
	
	public Map<String,String> ToHashMap()
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("Course", this.course);
		result.put("Year", Integer.toString(this.year));
		result.put("Week range", this.weeks);
		
		return result;
	}
	
	public String toString(AppSettings settings)
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
	
	public boolean isCourseDataSpecified()
	{
		return TextUtils.isEmpty(course) == false && TextUtils.isEmpty(weeks) == false && year > 0;
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
