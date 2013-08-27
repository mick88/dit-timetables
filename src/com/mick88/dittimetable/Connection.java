package com.mick88.dittimetable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class Connection
{	
	String receivedHtml;
	DefaultHttpClient httpClient=null;
	private Cookie cookie=null;
	public static String downloadsFolder = "Download";
	final AppSettings settings;
	public final static String
		ROOT_ADDRESS = "https://www.dit.ie/timetables/",
		ROOT_ADDRESS_PDF = "http://www.dit.ie/timetables/",
		WEBSITE_ADDRESS = ROOT_ADDRESS+"PortalServ",
			//"http://webtimetables.dit.ie/TTSuiteRBLIVE/PortalServ",
		LOGIN_ADDRESS = WEBSITE_ADDRESS;
	
/*	@Deprecated
	String username="",
			password="";*/
	
	final String logName = "TimetableConnection";
	
	@Deprecated
	public void setPassword(String password)
	{
//		this.password = password;
	}
	
	public boolean areCredentialsPresent()
	{
		return TextUtils.isEmpty(settings.getUsername())==false && TextUtils.isEmpty(settings.getPassword())==false;
	}
	
	public Cookie getCookie()
	{
		if (cookie == null) logIn();
		return cookie;
	}
	
	private void logIn()
	{
		Log.d(logName, "Getting cookie...");
		cookie = new Cookie("");
		try
		{
			/*Prepare request*/
			HttpPost post = new HttpPost(LOGIN_ADDRESS);
			List<NameValuePair> loginDetails = new ArrayList<NameValuePair>();
				loginDetails.add(new BasicNameValuePair("reqtype", "login"));
				loginDetails.add(new BasicNameValuePair("type", "null"));
				loginDetails.add(new BasicNameValuePair("appname", "unknown"));
				loginDetails.add(new BasicNameValuePair("appversion", "unknown"));
				loginDetails.add(new BasicNameValuePair("ostype", "Android"));
				loginDetails.add(new BasicNameValuePair("username", settings.getUsername()));
				loginDetails.add(new BasicNameValuePair("userpassword", settings.getPassword()));
				loginDetails.add(new BasicNameValuePair("Login", "1"));
				
			post.setEntity(new UrlEncodedFormEntity(loginDetails));
			
			/*Execute request*/
			HttpResponse response = httpClient.execute(post);

			if (response.getStatusLine().getStatusCode() < 400)
			{				
				Header h = response.getFirstHeader("Set-Cookie");
				if (h == null) cookie = new Cookie("");
				else cookie = new Cookie(h.getValue());
//				Log.d(logName, "Cookie: "+result.toString());
			}
			else 
			{
				Log.e(logName, "Cookie could not be fetched!");
			}
			response.getEntity().consumeContent();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	@SuppressLint("NewApi")
	public void downloadPdf(Context context, String address, String filename)
	{
		Uri uri = Uri.parse(address);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,  uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
		else
		{
			DownloadManager.Request request = new Request(uri);
			request.setDescription("DIT Timetable");
			request.setTitle("Timetable");
			MimeTypeMap m = MimeTypeMap.getSingleton();
			request.setMimeType(m.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(filename)));
	
			request.setDestinationInExternalPublicDir(downloadsFolder, filename);
	
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				setRequestAdvancedOptions(request);
			}
			DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
		}
	}
	
	@SuppressLint("NewApi")
	void setRequestAdvancedOptions(DownloadManager.Request request)
	{
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.allowScanningByMediaScanner();
	}
	
	public String getContent(String address)
	{
		if (cookie == null || cookie.isValid() == false) logIn();
		String query = WEBSITE_ADDRESS+address;
		Log.d(logName, "Query addr: "+query);		
		
		try
		{
			HttpGet get = new HttpGet(query);
			get.setHeader("Cookie", cookie.cookie);
			HttpResponse response = httpClient.execute(get);
			InputStream stream = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
			{
				builder.append(line);
			}
			
			reader.close();
			stream.close();
			response.getEntity().consumeContent();
			return builder.toString();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		
		return null;
	}
	
	
	@SuppressLint("NewApi")
	public Connection(AppSettings settings)
	{
		this.settings = settings;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
		{
			downloadsFolder = Environment.DIRECTORY_DOWNLOADS;
		}
		httpClient = new DefaultHttpClient();
	}
}