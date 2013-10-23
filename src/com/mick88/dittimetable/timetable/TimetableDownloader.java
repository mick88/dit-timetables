package com.mick88.dittimetable.timetable;

import java.io.FileInputStream;
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
import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.timetable.Exceptions.ServerConnectionException;
import com.mick88.dittimetable.web.Connection;

public abstract class TimetableDownloader extends AsyncTask<Void, Integer, RuntimeException>
{
	private static final String logTag = "TimetableDownloader";
	final Connection connection;
	final AppSettings appSettings;
	final Timetable timetable;
	final Context context;
	
	public TimetableDownloader(Context context,	Timetable timetable)
	{
		this.appSettings = timetable.getSettings();
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
	
	/** 
	 * Gets the timetable url
	 * @return address to the web timetable
	 */
	public String getQueryAddress(Timetable timetable)
	{
		if (timetable.weekRange == -1) return String.format(Locale.ENGLISH, 
				"?reqtype=timetable&action=getgrid&sKey=%s%%7C%s&sTitle=Computing&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&sWeeks=%s&sType=course&instCode=-2&instName=", 
				getDataset(), timetable.course, timetable.year, timetable.weeks);
		else return String.format(Locale.ENGLISH, 
				"?reqtype=timetable&action=getgrid&sKey=%s%%7C%s&sTitle=Computing&sYear=%d&sEventType=&sModOccur=&sFromDate=&sToDate=&weekRange=%d&sType=course&instCode=-2&instName=", 
				getDataset(), timetable.course, timetable.year, timetable.weekRange);
	}
	
	public static String getDataset()
	{
		Calendar c = Calendar.getInstance();
		int startYear=0;
		
		if (Timetable.getSemester() == 1)
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
		
		boolean result = parseGrid(string);
		
		if (result == false) 
		{
			throw new Exceptions.InvalidDataException();
		}
		
		// finalize by saving to file and downloading detailde info
		if (result == true) 
		{
			for (TimetableDay day : timetable.days)
			{
				for (TimetableEvent event : day.events)
				{
					if (isCancelled()) break;
					if (event.isUpdated()) continue;
					try
					{
						downloadAdditionalInfo(event);
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			timetable.exportTimetable(context);
			Log.i(logTag, "Timetable successfully downloaded");
			timetable.lastUpdated = new Date();
		}
	}
	
	
	/**
	 * Loads additional information about event from web timetables.
	 * @throws IOException 
	 */
	public void downloadAdditionalInfo(TimetableEvent event) throws IOException
	{
		Connection connection = timetable.getConnection();
		String uri = String.format(Locale.getDefault(), "?reqtype=eventdetails&eventId=%s%%7C%d", TimetableDownloader.getDataset(), event.id);

		String content = connection.getContent(uri);
		if (parseAdditionalInfo(event, content))
		{
			event.complete = true;
			event.updated = true;
			try
			{
				event.saveAdditionalInfo(context, content);
			} catch (IOException e)
			{
			
				e.printStackTrace();
			}
		}
	}
	
	public boolean parseGridRow(TimetableDay day, Elements gridCols)
	{
		TimetableEvent event = new TimetableEvent(day.id, gridCols);
		if (event.isValid())
		{			
				try
				{
					if (loadAdditionalInfo(event) == false)
						downloadAdditionalInfo(event);
					day.addClass(event);
					return true;
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			
			
		}
		else 
			return false;
	}
	
	public boolean loadAdditionalInfo(TimetableEvent event)
	{
		String filename = event.getFileName();	
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
						event.lecturer = parseLecturerName(elements.get(++i).text());
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
	
	
	public boolean parseGrid(String html)
	{
		// put days in a hashmap
		Map<String, TimetableDay> days = new HashMap<String, TimetableDay>(7);
		for (TimetableDay day : timetable.days)
			days.put(day.getShortName().toString(), day);
		
		int numParsedEvents = 0,
				totalEvents=0;
		timetable.clearEvents();
				
		Document document = Jsoup.parse(html);
		Elements gridRows = document.select("table.gridTable tr");
		if (gridRows == null || gridRows.isEmpty()) return false;
		totalEvents = gridRows.size();

		int currentRow=0;
		for (Element row : gridRows)
		{
			Elements columns = row.select("td.gridData");
			this.publishProgress(++currentRow, totalEvents);
			if (columns.isEmpty()) continue;
			String day = columns.get(TimetableEvent.GRID_DAY).text();
			TimetableDay tDay = days.get(day);
			if (tDay != null)
			{
				if (parseGridRow(tDay, columns))
					numParsedEvents++;
			}
		}
		
		for (TimetableDay day : timetable.days)
			day.sortEvents();
		
		timetable.valid = (numParsedEvents > 0);
		return timetable.valid;
		
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
		onTimetableDownloaded(timetable, result);
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if (values.length > 1)
			onDownloadProgress(values[0], values[1]);
	}
	
	protected abstract void onTimetableDownloaded(Timetable timetable, RuntimeException exception);
	protected abstract void onDownloadProgress(int progress, int max);
}
