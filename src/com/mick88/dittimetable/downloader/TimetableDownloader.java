package com.mick88.dittimetable.downloader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.mick88.dittimetable.downloader.Exceptions.ServerConnectionException;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;
import com.mick88.dittimetable.timetable.TimetableEvent.ClassType;

public class TimetableDownloader extends AsyncTask<Void, Integer, RuntimeException>
{
	public static interface TimetableDownloadListener
	{
		void onTimetableDownloaded(Timetable timetable, RuntimeException exception);
		void onDownloadProgress(int progress, int max);
	}
	
	private static class DownloadStateSaver implements TimetableDownloadListener
	{
		RuntimeException exception;
		Timetable timetable;
		int progress, maxProgress;
		
		@Override
		public void onTimetableDownloaded(Timetable timetable,
				RuntimeException exception)
		{
			this.exception = exception;
			this.timetable = timetable;
		}

		@Override
		public void onDownloadProgress(int progress, int max)
		{
			this.progress = progress;
			this.maxProgress = max;			
		}
		
		// update another listener with saved data
		void readState(TimetableDownloadListener listener)
		{
			if (timetable != null)
				listener.onTimetableDownloaded(timetable, exception);
			else if (maxProgress > 0)
				listener.onDownloadProgress(progress, maxProgress);
		}
	}
	
	private static final String logTag = "TimetableDownloader";
	final Connection connection;
	final AppSettings appSettings;
	final Timetable timetable;
	final Context context;
	TimetableDownloadListener timetableDownloadListener = new DownloadStateSaver();
	
	public TimetableDownloader(Context context,	Timetable timetable, AppSettings settings)
	{
		this.appSettings = settings;
		this.connection = new Connection(appSettings);
		this.timetable = timetable;
		this.context = context;
	}
	
	public TimetableDownloader(Context context, AppSettings appSettings)
	{
		this.connection = new Connection(appSettings);
		this.appSettings = appSettings;
		this.timetable = new Timetable(appSettings);
		this.context = context;
	}
	
	public TimetableDownloader setTimetableDownloadListener(
			TimetableDownloadListener timetableDownloadListener)
	{
		if (timetableDownloadListener == null)
			this.timetableDownloadListener = new DownloadStateSaver();
		else
		{
			if (this.timetableDownloadListener instanceof DownloadStateSaver)
				((DownloadStateSaver) this.timetableDownloadListener).readState(timetableDownloadListener);
			this.timetableDownloadListener = timetableDownloadListener;
		}
		
		return this;		
	}
	
	/** 
	 * Gets the timetable url
	 * @return address to the web timetable
	 */
	public String getQueryAddress(Timetable timetable)
	{
		if (timetable.getWeekRangeId() == -1) return String.format(Locale.ENGLISH, 
				"?reqtype=timetable&action=getgrid&sKey=%s%%7C%s&sTitle=Computing&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&sWeeks=%s&sType=course&instCode=-2&instName=", 
				getDataset(), timetable.getCourse(), timetable.getYear(), timetable.getWeekRange());
		else return String.format(Locale.ENGLISH, 
				"?reqtype=timetable&action=getgrid&sKey=%s%%7C%s&sTitle=Computing&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&weekRange=%d&sType=course&instCode=-2&instName=", 
				getDataset(), timetable.getCourse(), timetable.getYear(), timetable.getWeekRangeId());
	}
	
	public static String getDataset()
	{
		Calendar c = Calendar.getInstance();
		int startYear=0;
		
		if (Timetable.getCurrentSemester() == 1)
			startYear = c.get(Calendar.YEAR);
		else
			startYear = c.get(Calendar.YEAR)-1;
		
		StringBuilder builder = new StringBuilder(6);
		builder.append(startYear);
		builder.append((startYear+1) % 100);
		return builder.toString();
	}
	
	/**
	 * Downloads timetable content from web timetables - graphic view
	 * @param context
	 * @return true if successful
	 */
	public void download()
	{
		if (connection.areCredentialsPresent() == false)
			throw new Exceptions.SettingsEmptyException();

		String query = getQueryAddress(timetable);
		
		String string;
		try
		{
			string = connection.getContent(query);
		} 
		catch (ServerConnectionException e)
		{
			throw new Exceptions.TimetableException(e.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new Exceptions.ServerConnectionException();
		}
		
		if (string == null)
			throw new Exceptions.ServerConnectionException();
		
		if (string.contains("You must login again."))
			throw new Exceptions.SessionExpiredException();
		
		if (string.contains("There are no events to display."))
			throw new Exceptions.NoEventsException();
		
		if (string.contains("The system is loading commonly used data at the moment"))
			throw new Exceptions.ServerLoadingException();
		
		if (string.contains("Your login details are incorrect. Please attempt login again."))
			throw new Exceptions.IncorrectCredentialsException();
		
		if (string.contains("There are no events in the selected timetable."))
			throw new Exceptions.EmptyTimetableException();
		
		if (string.contains("There are no course records matching the criteria."))
			throw new Exceptions.WrongCourseException();
		
		parseGrid(string);
		
		timetable.exportTimetable(context);
		Log.i(logTag, "Timetable successfully downloaded");
		timetable.setLastUpdated(new Date());
	}
	
	
	/**
	 * Loads additional information about event from web timetables.
	 * @throws IOException 
	 */
	public void downloadAdditionalInfo(TimetableEvent event) throws IOException
	{
		String uri = String.format(Locale.getDefault(), "?reqtype=eventdetails&eventId=%s%%7C%d", TimetableDownloader.getDataset(), event.getId());

		String content = connection.getContent(uri);
		if (parseAdditionalInfo(event, content))
		{
			event.setComplete(true);
		}
	}
	
	public boolean parseGridRow(TimetableDay day, Elements gridCols)
	{
		TimetableEvent event = parseEvent(day, gridCols);
		if (event.isValid())
		{			
			day.addEvent(event);
			try
			{
				downloadAdditionalInfo(event);
				
				return true;
			} catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else 
			return false;
	}
	
	// Grid columns for event info
	public static final int
		GRID_ID = 1,
		GRID_DAY = 2,
		GRID_TIME_START = 3,
		GRID_TIME_FINISH = 4,
		GRID_ROOM = 5,
		GRID_MODULE_CODE = 7,
		GRID_MODULE_NAME = 8,
		GRID_EVENT_TYPE = 9;
	
	private TimetableEvent parseEvent(TimetableDay day, Elements gridColumns)
	{
		TimetableEvent event = new TimetableEvent(day.getId());
		event.setId(Integer.parseInt(gridColumns.get(GRID_ID).text()));
		
		int [] time = parseHour(gridColumns.get(GRID_TIME_START).text());
		event.setStartHour(time[0]);
		event.setStartMin(time[1]);
		
		time = parseHour(gridColumns.get(GRID_TIME_FINISH).text());
		event.setEndHour(time[0]);
		event.setEndMin(time[1]);
		
		event.setRoom(parseRooms(gridColumns.get(GRID_ROOM).text()));
		event.setName(parseModuleName(gridColumns.get(GRID_MODULE_NAME).text()));
		event.setType(parseType(gridColumns.get(GRID_EVENT_TYPE).text()));
		
		return event;
	}
	
	/**
	 * Gets filename for event chaced data
	 * @return
	 */
	protected String getEventFileName(TimetableEvent event)
	{
		return String.format(Locale.getDefault(), "%d.html", event.getId());
	}
	
	protected void saveAdditionalInfo(TimetableEvent event, String content) throws IOException
	{
		String filename = getEventFileName(event);
		FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);			
		byte[] buffer = content.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}
	
	public boolean loadAdditionalInfo(TimetableEvent event)
	{
		String filename = getEventFileName(event);	
		StringBuffer sb = new StringBuffer();
		try
		{
			if (context.getFileStreamPath(filename).exists() == false)
			{
				Log.i(logTag, "Event  could not be loaded from file. File does not exist "+filename);
				return false;
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
			return false;
		}
		
		if (parseAdditionalInfo(event, sb.toString()))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * parses the additional info page
	 */
	private boolean parseAdditionalInfo(TimetableEvent event, String content)
	{
		if (content == null) return false;
		
		try
		{
			Document doc = Jsoup.parse(content);
			
			Elements elements = doc.select("th, td");		
			for (int i=0; i < elements.size(); i++)
			{
				Element element = elements.get(i);
				if (element.tagName().equalsIgnoreCase("th"))
				{
					String headerText = element.text();
					if (headerText.equals("Class Subgroup"))
					{
						event.setGroups(elements.get(++i).text());
					} 
					else if (headerText.equals("Week numbers"))
					{
						event.setWeekRange(elements.get(++i).text());
					}
					else if (headerText.equals("Lecturer"))
					{
						event.setLecturer(parseLecturerName(elements.get(++i).text()));
					} 
				}
			}
		}
		catch (Exception e)
		{
			FlurryAgent.onError("parseAdditionalInfo", "TimetableEvent", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String parseLecturerName(String text)
	{
		String [] parts = text.split(" - ");
		if (parts.length > 1)
			return parts[parts.length - 1];
		else 
			return text;
	}
	
	
	public void parseGrid(String html)
	{
		// put days in a hashmap
		Map<String, TimetableDay> days = new HashMap<String, TimetableDay>(7);
		for (TimetableDay day : timetable.getDays())
			days.put(day.getShortName().toString(), day);
		
		int numParsedEvents = 0,
				totalEvents=0;
		timetable.clearEvents();
				
		Document document = Jsoup.parse(html);
		Elements gridRows = document.select("table.gridTable tr");
		if (gridRows == null || gridRows.isEmpty())
			throw new Exceptions.InvalidDataException();
		totalEvents = gridRows.size();

		int currentRow=0;
		for (Element row : gridRows)
		{
			Elements columns = row.select("td.gridData");
			this.publishProgress(++currentRow, totalEvents);
			if (columns.isEmpty()) continue;
			String day = columns.get(GRID_DAY).text();
			TimetableDay tDay = days.get(day);
			if (tDay != null)
			{
				if (parseGridRow(tDay, columns))
					numParsedEvents++;
			}
		}
		
		for (TimetableDay day : timetable.getDays())
			day.sortEvents();	
	}

	@Override
	protected RuntimeException doInBackground(Void... params)
	{
		try
		{
			download();
			return null;
		}
		catch (RuntimeException exception)
		{
			return exception;
		}
	}
	
	@Override
	protected void onPostExecute(RuntimeException result)
	{
		super.onPostExecute(result);
		timetableDownloadListener.onTimetableDownloaded(timetable, result);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if (values.length > 1)
			timetableDownloadListener.onDownloadProgress(values[0], values[1]);
	}
	
	private static String parseModuleName(String s)
	{
		String stripped = stripCurlyBraces(s);
		String [] parts = stripped.split(",");
		if (parts.length == 0) return s;
		else return parts[0];
	}
	
	private static String parseRooms(String text)
	{
		return stripCurlyBraces(text);
	}
	
	private static String stripCurlyBraces(String text)
	{
		int start = text.indexOf('{')+1;
		if (start > 0)
		{
			int end = text.indexOf('}', start);
			if (end > -1)
				return text.substring(start, end);
		}
		return text;
	}
	
	/**
	 * return hour as integer from hh:mm string
	 */
	private static int [] parseHour(String time)
	{
		String [] parts = time.split(":");
		int [] result = new int [parts.length];
		for (int i=0; i < parts.length; i++)
			result[i] = Integer.parseInt(parts[i]);
		return result;
	}
	
	private static ClassType parseType(String s)
	{
		try
		{
			return Enum.valueOf(ClassType.class, s);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			return ClassType.Other;
		}
	}
	
	public static String [] getCourseCodes()
	{
		return new String [] {
				"DT001",
				"DT002",
				"DT003",
				"DT003A",
				"DT004",
				"DT005",
				"DT006",
				"DT007",
				"DT008",
				"DT009",
				"DT010",
				"DT011",
				"DT011P",
				"DT012",
				"DT020",
				"DT021",
				"DT022",
				"DT023",
				"DT024",
				"DT025",
				"DT026",
				"DT027",
				"DT028",
				"DT032",
				"DT033",
				"DT036A",
				"DT039",
				"DT040",
				"DT041",
				"DT080",
				"DT080A",
				"DT081",
				"DT086",
				"DT087",
				"DT088",
				"DT089",
				"DT089A",
				"DT090",
				"DT090A",
				"DT090E",
				"DT091",
				"DT092",
				"DT093",
				"DT097A",
				"DT101",
				"DT104",
				"DT105",
				"DT106",
				"DT107",
				"DT110",
				"DT111",
				"DT112",
				"DT113",
				"DT117",
				"DT118",
				"DT118C",
				"DT121",
				"DT121A",
				"DT122",
				"DT123",
				"DT124",
				"DT133",
				"DT134",
				"DT149A",
				"DT155",
				"DT159",
				"DT164",
				"DT168",
				"DT169",
				"DT170",
				"DT175",
				"DT175A",
				"DT201",
				"DT203",
				"DT204",
				"DT205",
				"DT206",
				"DT207",
				"DT208",
				"DT209",
				"DT211",
				"DT212",
				"DT217",
				"DT217A",
				"DT218",
				"DT220",
				"DT221",
				"DT222",
				"DT223",
				"DT224",
				"DT226",
				"DT226A",
				"DT227",
				"DT228",
				"DT228A",
				"DT228B",
				"DT229",
				"DT230",
				"DT230B",
				"DT231A",
				"DT233",
				"DT234",
				"DT235",
				"DT236P",
				"DT237",
				"DT238",
				"DT247",
				"DT248",
				"DT249",
				"DT259",
				"DT260",
				"DT261",
				"DT265",
				"DT276",
				"DT285",
				"DT286",
				"DT299",
				"DT299T",
				"DT303",
				"DT310",
				"DT314A",
				"DT315",
				"DT321",
				"DT324",
				"DT329",
				"DT331",
				"DT332",
				"DT340A",
				"DT341",
				"DT343",
				"DT344",
				"DT346",
				"DT347",
				"DT348",
				"DT349",
				"DT350",
				"DT351",
				"DT352",
				"DT353",
				"DT354",
				"DT355",
				"DT357",
				"DT358",
				"DT360",
				"DT364",
				"DT365",
				"DT366",
				"DT370",
				"DT374",
				"DT375",
				"DT388",
				"DT389",
				"DT390",
				"DT395",
				"DT398",
				"DT398A",
				"DT399",
				"DT401",
				"DT401T",
				"DT406",
				"DT406A",
				"DT406H",
				"DT406T",
				"DT407",
				"DT408A",
				"DT408H",
				"DT408T",
				"DT411",
				"DT411H",
				"DT412",
				"DT413",
				"DT414",
				"DT415",
				"DT416",
				"DT417",
				"DT418",
				"DT418P",
				"DT420",
				"DT421",
				"DT422",
				"DT423",
				"DT424",
				"DT425",
				"DT432",
				"DT432A",
				"DT434",
				"DT435",
				"DT436",
				"DT436T",
				"DT437",
				"DT438",
				"DT441",
				"DT442T",
				"DT443",
				"DT444",
				"DT444T",
				"DT451",
				"DT460E",
				"DT477",
				"DT480",
				"DT481",
				"DT491",
				"DT501",
				"DT502A",
				"DT504",
				"DT505",
				"DT506",
				"DT518",
				"DT519",
				"DT520",
				"DT522A",
				"DT522F",
				"DT527",
				"DT528",
				"DT529",
				"DT530",
				"DT531",
				"DT532",
				"DT533",
				"DT533M",
				"DT533N",
				"DT534",
				"DT539B",
				"DT540",
				"DT540A",
				"DT540B",
				"DT542",
				"DT543",
				"DT544",
				"DT545",
				"DT546",
				"DT547",
				"DT547A",
				"DT548",
				"DT549",
				"DT550",
				"DT552",
				"DT553",
				"DT555",
				"DT556",
				"DT557",
				"DT558",
				"DT559",
				"DT561",
				"DT564",
				"DT565",
				"DT567",
				"DT568",
				"DT569",
				"DT571",
				"DT572",
				"DT572A",
				"DT572B",
				"DT572H",
				"DT574",
				"DT576",
				"DT577",
				"DT582",
				"DT584",
				"DT586",
				"DT587",
				"DT589",
				"DT592",
				"DT596",
				"DT597",
				"DT598",
				"DT624",
				"DT704",
				"DT710",
				"DT711",
				"DT712",
				"DT715",
				"DT761",
				"DT764",
				"DT768",
				"DT789",
		};
	}
}
