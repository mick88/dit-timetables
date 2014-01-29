package com.mick88.dittimetable.timetable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.TextUtils;

import com.michaldabski.msqlite.Annotations.TableName;
import com.mick88.dittimetable.downloader.Connection;
import com.mick88.dittimetable.downloader.Exceptions;
import com.mick88.dittimetable.downloader.TimetableDownloader;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.utils.FileUtils;

/**
 * Class containing a timetable divided into days
 * @author Michal
 *
 */
@TableName("Timetable")
public class Timetable extends TimetableStub
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
			
	public static int getCurrentWeek()
	{
		Calendar cal = Calendar.getInstance();
		int startYear = (getCurrentSemester() == 1) ? cal.get(Calendar.YEAR) : (cal.get(Calendar.YEAR)-1);
		
		Date yearStart = new GregorianCalendar(startYear, START_MONTH, START_DAY).getTime();
		Date today = new GregorianCalendar().getTime();
		
		long milisBetween = today.getTime() - yearStart.getTime();
		int daysBetween = (int) (milisBetween / 1000 / 60 / 60 / 24);
		
		return (daysBetween / 7)+1;		
	}
	
	/**
	 * Gets current calendar semester
	 */
	public static int getCurrentSemester()
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
	@Deprecated
	private static final String DAY_SEPARATOR = ":day:";
	
	private int weekRangeId = INVALID_WEEK_RANGE; // alternative to weeks
	private TimetableDay[] days = new TimetableDay[NUM_DAYS];
	
	public Timetable()
	{
		super();
		for (int i = 0; i < NUM_DAYS; i++)
			this.getDays()[i] = new TimetableDay(i);
	}
	/**
	 * Creates new Timetable object from settings
	 */
	public Timetable(AppSettings settings)
	{
		this();
		this.course = settings.getCourse();
		this.setYear(settings.getYear());
		this.setWeekRange(settings.getWeekRange());			
	}
	
	/**
	 * Creates new tiemtable object with custom settings
	 */
	public Timetable(String course, int year, int weekRangeId)
	{
		this();		
		this.course = course;
		this.setYear(year);
		this.setWeekRangeId(weekRangeId);
	}	
	
	public Timetable(String course, int year, String weekRange)
	{
		this(course, year, INVALID_WEEK_RANGE);
		this.setWeekRange(weekRange);		
	}
	
	public void clearEvents()
	{
		for (int i=0; i < NUM_DAYS; i++)
		{
			getDays()[i].clearEvents();
		}
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
				TimetableDownloader.getDataset(), course, getYear(), getWeekRange());
		String string = connection.getContent(query);
		if (TextUtils.isEmpty(string)) 
			return null;
		
		Elements elements = Jsoup.parse(string).select("a[href$=.pdf]");
		Element pdfLink = elements.first();
		
		return  Connection.ROOT_ADDRESS_PDF + pdfLink.attr("href");
	}
	
	/**
	 * Generate filename for downloaded PDF
	 */
	public String getPdfFileName()
	{
		final String weeksStr;
		if (getWeekRange().equals(SEMESTER_1)) weeksStr = "S1";
		else if (getWeekRange().equals(SEMESTER_2)) weeksStr = "S2";
		else weeksStr = "W"+getWeekRange();
		
		return String.format(Locale.getDefault(), "Timetable_%s-%d_%s.pdf", course, getYear(), weeksStr);
	}
	
	/**
	 * Writes timetable to file
	 */
	@Deprecated
	public void exportTimetable(Context context)
	{
		StringBuilder builder = new StringBuilder();
		for (TimetableDay day : getDays())
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
	
	public TimetableDay getDay(int id)
	{
		return getDays()[id];
	}
	
	String getFilename()
	{
		return String.format(Locale.getDefault(), "%s_%d_%s_%s.txt", course, getYear(), getWeekRange(), TimetableDownloader.getDataset());
	}
	
	public Set<String> getGroupsInTimetable()
	{
		Set<String> groups = new HashSet<String>();
		for (TimetableDay day : getDays())
			day.getGroups(groups);
		return groups;
	}
	
	public void hideGroup(String groupCode, AppSettings settings)
	{
		if (groupCode.equals(getCourseYearCode())) return; //cannot hide whole class
		settings.hideGroup(groupCode);
	}
	
	/**
	 * Loads timetable from file
	 */
	@Deprecated
	public void importSavedTimetable(Context context)
	{
		if (importTimetable(context) == false) 
			throw new Exceptions.NoLocalCopyException();
	}
	
	@Deprecated
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
			if (dayId < getDays().length)
			{
				n += getDays()[dayId++].importFromString(d);
			}
		}
		return (n > 0);
	}
	
	public Map<String,String> ToHashMap()
	{
		Map<String, String> result = new HashMap<String, String>();
		result.put("Course", this.course);
		result.put("Year", Integer.toString(this.getYear()));
		result.put("Week range", this.getWeekRange());
		
		return result;
	}
	
	public String toString(AppSettings settings)
	{
		StringBuilder builder= new StringBuilder();
		int n=0;
		for (TimetableDay day : getDays())
		{
			if (day.isEmpty(settings)) continue;
			String s = day.toString(settings);
			if (TextUtils.isEmpty(s)) continue;
			
			if (n > 0) builder.append("\n\n");
			builder.append(s);			
			n++;
		}
		return builder.toString();
	}
	
	@Override
	public String toString()
	{
		return describe().toString();
	}
	
	@Deprecated
	public void writeFile(Context context, String filename, String content) throws IOException
	{
		FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);			
		byte[] buffer = content.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}

	public int getWeekRangeId()
	{
		return weekRangeId;
	}

	public void setWeekRangeId(int weekRangeId)
	{
		this.weekRangeId = weekRangeId;
	}

	public TimetableDay[] getDays()
	{
		return days;
	}

	public void setDays(TimetableDay[] days)
	{
		this.days = days;
	}
	
	/**
	 * Gets set of names of all modules in this timetable
	 */
	public Set<String> getModuleNames()
	{
		Set<String> result = new HashSet<String>();
		for (TimetableDay day : days)
			for (TimetableEvent event : day.getEvents())
				result.add(event.getName());
		return result;
	}
	
	public boolean isEmpty()
	{
		for (TimetableDay day : days)
			if (day.isEmpty() == false)
				return false;
		return true;
	}
}
