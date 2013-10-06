package com.mick88.dittimetable.pdf_download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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
import com.mick88.dittimetable.utils.HttpUtils;

public class PdfDownloaderService extends Service
{
	private static final String ACCEPTED_TYPE = "application/pdf";

	private static interface onDownloadProgressListener
	{
		void onProgress(int progress, int max);
	}
	
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
		new AsyncTask<Timetable, Integer, File>()
		{
			Builder progressNotification;
			protected void onPreExecute() 
			{
				progressNotification = new Builder(getApplicationContext())
					.setSmallIcon(R.drawable.ic_notification_download)
					.setTicker("Downloading timetable...")
					.setContentTitle("DIT Timetables")
					.setProgress(100, 0, true)
					.setContentText("Downloading PDF...");
				startForeground(NOTIFICATION_ID, progressNotification.build());
			}
			
			@Override
			protected void onProgressUpdate(Integer... values) 
			{
				progressNotification.setProgress(values[1], values[0], false);
				startForeground(NOTIFICATION_ID, progressNotification.build());
			}

			@Override
			protected File doInBackground(Timetable... params)
			{
				Timetable timetable = params[0];
				String url = timetable.getPdfUrl();
				String filename = timetable.getPdfFileName();
				File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				File file = new File(folder, filename);
				try
				{
					if (downloadFile(url, file, new onDownloadProgressListener()
					{
						
						@Override
						public void onProgress(int progress, int max)
						{
							onProgressUpdate(progress, max);							
						}
					}) == 0)
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
						.setSmallIcon(R.drawable.ic_notification_download)
						.setTicker("Timetable downloaded")
						.setContentIntent(PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, intent, 0))
						.setContentTitle("Dit Timetables")
						.setContentText("Timetable downloaded");
					NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(NOTIFICATION_ID, builder.build());
				}
				else
				{
					Builder builder = new Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_notification_download)
						.setContentTitle("Dit Timetables")
						.setTicker("PDF download error!")
						.setContentText("PDF download error!");
					NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(NOTIFICATION_ID, builder.build());
				}
						
				stopSelf();
			}
			
		}.execute(timetable);
	}
	
	private int downloadFile(String url, File outputFile, onDownloadProgressListener progressListener) throws IOException
	{
		final int BUFFER_SIZE = 1024;
		Log.d("PDF Downloader", "Downloading file "+url);
		
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, HttpUtils.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, HttpUtils.CONNECTION_TIMEOUT);
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept-Type", ACCEPTED_TYPE);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		
		HttpResponse response = httpClient.execute(httpGet);
		Log.d("PDF Service", "Response: "+response.getStatusLine().toString());
		HttpEntity entity = response.getEntity();
		
		int length = (int) entity.getContentLength();
		
		if (length == 0)
			throw new IOException("Content length is 0");
		
		String type = response.getFirstHeader("Content-Type").getValue();
		if (type.equalsIgnoreCase(ACCEPTED_TYPE) == false)
		{
			throw new IOException("Downloaded file is not PDF");
		}
	
		InputStream inStream = new BufferedInputStream(entity.getContent());
		OutputStream outStream = new FileOutputStream(outputFile);
		
		byte [] data = new byte [BUFFER_SIZE];
		int count;
		int total=0;
		while ((count = inStream.read(data)) != -1)
		{
			total += count;
			outStream.write(data, 0,  count);
		}
		outStream.flush();
		outStream.close();
		inStream.close();
		
		entity.consumeContent();
		
		return total;
	}
}
