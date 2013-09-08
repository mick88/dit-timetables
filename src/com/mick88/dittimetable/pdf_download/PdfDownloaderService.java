package com.mick88.dittimetable.pdf_download;

import com.mick88.dittimetable.timetable.Timetable;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

public class PdfDownloaderService extends Service
{
	public static final String EXTRA_TIMETABLE = "timetable";
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		downloadPdf((Timetable) intent.getSerializableExtra(EXTRA_TIMETABLE));
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void downloadPdf(Timetable timetable)
	{
		new AsyncTask<Timetable, Void, Void>()
		{

			@Override
			protected Void doInBackground(Timetable... params)
			{
				Timetable timetable = params[0];
				String url = timetable.getPdfUrl();
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) 
			{
				stopSelf();
			}
			
		}.execute(timetable);
	}
}
