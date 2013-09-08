package com.mick88.dittimetable.pdf_download;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.Timetable;

public class PdfDownloaderService extends Service
{
	private static final int NOTIFICATION_ID = 0;
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
			protected void onPreExecute() 
			{
				Builder builder = new Builder(getApplicationContext())
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("DIT Timetables")
					.setContentText("Downloading PDF");
				startForeground(NOTIFICATION_ID, builder.build());
			}

			@Override
			protected Void doInBackground(Timetable... params)
			{
				Timetable timetable = params[0];
				String url = timetable.getPdfUrl();
				
				return null;
			}
			
			@Override
			protected void onCancelled(Void result) 
			{
				// TODO: clean if necessary
				stopForeground(true);
			}
			
			@Override
			protected void onPostExecute(Void result) 
			{
				stopForeground(true);
				
				Builder builder = new Builder(getApplicationContext())
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Dit Timetables")
					.setContentText("Timetable downloaded");
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(NOTIFICATION_ID, builder.build());
						
				stopSelf();
			}
			
		}.execute(timetable);
	}
}
