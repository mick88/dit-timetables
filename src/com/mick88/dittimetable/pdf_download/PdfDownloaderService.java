package com.mick88.dittimetable.pdf_download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.mick88.dittimetable.R;
import com.mick88.dittimetable.timetable.Timetable;

public class PdfDownloaderService extends Service
{
	private static final int NOTIFICATION_ID = 1;
	public static final String EXTRA_TIMETABLE = "timetable";
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent != null)
		{
			Object t = intent.getSerializableExtra(EXTRA_TIMETABLE);
			if (t instanceof Timetable)
				downloadPdf((Timetable) t);
		}
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
		new AsyncTask<Timetable, Void, File>()
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
			protected File doInBackground(Timetable... params)
			{
				Timetable timetable = params[0];
				String url = timetable.getPdfUrl();
				String filename = timetable.getPdfFileName();
				File file = new File(Environment.getExternalStorageDirectory(), filename);
				try
				{
					if (downloadFile(url, file) == 0)
						return null;
					return file;
				} 
				catch (IOException e)
				{
					e.printStackTrace();
					return null;
				}
			}
			
			@Override
			protected void onCancelled(File result) 
			{
				// TODO: clean if necessary
				stopForeground(true);
			}
			
			@Override
			protected void onPostExecute(File result) 
			{
				stopForeground(true);
				
				if (result != null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.fromFile(result));
					Builder builder = new Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentIntent(PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, intent, 0))
						.setContentTitle("Dit Timetables")
						.setContentText("Timetable downloaded");
					NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(NOTIFICATION_ID, builder.build());
				}
				else
				{
					Builder builder = new Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle("Dit Timetables")
						.setContentText("PDF download error!");
					NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(NOTIFICATION_ID, builder.build());
				}
						
				stopSelf();
			}
			
		}.execute(timetable);
	}
	
	private int downloadFile(String url, File outputFile) throws IOException
	{
		final int BUFFER_SIZE = 1024;
		Log.d("PDF Downloader", "Downloading file "+url);
		URL pdfUrl = new URL(url);
		URLConnection connection = pdfUrl.openConnection();
		connection.setDoOutput(true);
		connection.connect();
		int length = connection.getContentLength();
		
		if (length == 0)
			throw new IOException("Conten length is 0");
	
		InputStream inStream = connection.getInputStream();
		OutputStream outStream = new FileOutputStream(outputFile);
		
		byte [] data = new byte [BUFFER_SIZE];
		int count;
		int total=0;
		while ((count = inStream.read(data)) != -1)
		{
			total += count;
			outStream.write(data);
		}
		outStream.flush();
		outStream.close();
		inStream.close();
		
		return total;
	}
}
