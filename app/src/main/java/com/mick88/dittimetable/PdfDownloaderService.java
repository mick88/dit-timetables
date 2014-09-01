package com.mick88.dittimetable;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.mick88.dittimetable.downloader.Connection;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable_activity.TimetableActivity;
import com.mick88.dittimetable.utils.HttpUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@TargetApi(Build.VERSION_CODES.FROYO)
public class PdfDownloaderService extends Service
{
	private static final int INTENT_ID_VIEW_PDF = 3;
	private static final int INTENT_ID_ERRORMSG = 2;
	private static final int INTENT_ID_SHARE_URL = 1;
	private static final int INTENT_ID_SHARE_PDF = 0;
	private static final String ACCEPTED_TYPE = "application/pdf";	
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
		new AsyncTask<Timetable, Exception, File>()
		{
			Builder progressNotification;
			String url;
			protected void onPreExecute() 
			{
				progressNotification = new Builder(getApplicationContext())
					.setSmallIcon(R.drawable.ic_notification_download)
					.setTicker(getString(R.string.downloading_timetable_))
					.setContentTitle(getString(R.string.dit_timetables))
					.setProgress(100, 0, true)
					.setContentIntent(PendingIntent.getActivity(getApplicationContext(), INTENT_ID_ERRORMSG, new Intent(getApplicationContext(), TimetableActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
					.setContentText(getString(R.string.downloading_timetable_));
				startForeground(NOTIFICATION_ID, progressNotification.build());
			}
			
			@Override
			protected void onProgressUpdate(Exception... values) 
			{
				stopForeground(true);
				showErrorNotification(values[0].getMessage());
			}

			@Override
			protected File doInBackground(Timetable... params)
			{
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false)
				{
					publishProgress(new IOException("External storage not detected!"));
				}
				
				Timetable timetable = params[0];
				try
				{
					url = timetable.getPdfUrl(new Connection(AppSettings.loadFromPreferences(getApplicationContext())));
				}
				catch (Exception e)
				{
					publishProgress(new IOException("Server not responding"));
					return null;
				}
				String filename = timetable.getPdfFileName();
				File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				if (folder.exists() == false)
					folder.mkdirs();
				File file = new File(folder, filename);
				try
				{
					if (downloadFile(url, file) == 0)
						throw new Exception("Received stream is empty");
					
					return file;
				} 
				catch (Exception e)
				{
					e.printStackTrace();
					publishProgress(e);
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
					onPdfDownloaded(result, url);
				}
						
				stopSelf();
			}
			
		}.execute(timetable);
	}
	
	void showErrorNotification(String message)
	{
		Intent intent = new Intent(getApplicationContext(), TimetableActivity.class)
			.putExtra(TimetableActivity.EXTRA_ERROR_MESSAGE, message)
			.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		Builder builder = new Builder(getApplicationContext())
			.setSmallIcon(R.drawable.ic_notification_download_error)
			.setContentTitle(getString(R.string.dit_timetables))
			.setTicker(getString(R.string.pdf_download_error_))
			.setContentIntent(PendingIntent.getActivity(getApplicationContext(), INTENT_ID_ERRORMSG, intent, PendingIntent.FLAG_UPDATE_CURRENT))
			.setContentText(message);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
	
	void onPdfDownloaded(File pdfFile, String url)
	{
		Intent shareIntent = new Intent(Intent.ACTION_SEND)
			.setType(ACCEPTED_TYPE)
			.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pdfFile))
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent sharePendingIntent = PendingIntent.getActivity(getApplicationContext(), INTENT_ID_SHARE_PDF, Intent.createChooser(shareIntent, getString(R.string.share_timetable_pdf)), PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent shareUrlIntent = new Intent(Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(Intent.EXTRA_TEXT, url)
			.putExtra(Intent.EXTRA_TITLE, "Timetable")
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent shareUrlPendingIntent = PendingIntent.getActivity(getApplicationContext(), INTENT_ID_SHARE_URL, Intent.createChooser(shareUrlIntent, getString(R.string.share_pdf_url)), PendingIntent.FLAG_UPDATE_CURRENT);
			
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.fromFile(pdfFile));
		
		Builder builder = new Builder(getApplicationContext())
			.setSmallIcon(R.drawable.ic_notification_download_complete)
			.setTicker(getString(R.string.timetable_pdf_downloaded))
			.setContentIntent(PendingIntent.getActivity(getApplicationContext(), INTENT_ID_VIEW_PDF, intent, 0))
			.setContentTitle(getString(R.string.dit_timetables))
			.addAction(R.drawable.ic_notification_share, getString(R.string.share_pdf), sharePendingIntent)
			.addAction(R.drawable.ic_notification_share_url, getString(R.string.share_url), shareUrlPendingIntent)
			.setContentText(getString(R.string.timetable_pdf_downloaded));
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
	
	private int downloadFile(String url, File outputFile) throws IOException
	{
		final int BUFFER_SIZE = 1024;
		Log.d("PDF Downloader", "Downloading file "+url);
		
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, HttpUtils.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, HttpUtils.CONNECTION_TIMEOUT);
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept-Type", ACCEPTED_TYPE);
		httpGet.setHeader("User-Agent", Connection.USER_AGENT);
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
