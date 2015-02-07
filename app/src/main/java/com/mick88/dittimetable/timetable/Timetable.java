package com.mick88.dittimetable.timetable;

import android.text.TextUtils;

import com.michaldabski.msqlite.Annotations.TableName;
import com.mick88.dittimetable.downloader.Connection;
import com.mick88.dittimetable.downloader.TimetableDownloader;
import com.mick88.dittimetable.settings.AppSettings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
		START_MONTH = Calendar.SEPTEMBER,
		START_DAY = 1;
	
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
			return (day > DAY_SATURDAY) ? DAY_MONDAY : day;
		else 
			return (day > DAY_SATURDAY) ? -1 : day;
    }
	
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
	
	public TimetableDay getDay(int id)
	{
        if (id == -1) return null;
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

	public TimetableDay getToday(boolean defaultMonday)
	{
		return getDay(getTodayId(defaultMonday));
	}
}
