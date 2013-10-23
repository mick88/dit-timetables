package com.mick88.dittimetable.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.mick88.dittimetable.AppSettings;
import com.mick88.dittimetable.utils.HttpUtils;

public class Connection implements Serializable
{
	private static final long serialVersionUID = 1L;

	String receivedHtml;
	public static final String USER_AGENT = "DIT Timetables app";
	private Cookie cookie=null;
	public static String downloadsFolder = "Download";
	final AppSettings settings;
	public final static String
		ROOT_ADDRESS = "https://www.dit.ie/timetables/",
		ROOT_ADDRESS_PDF = "http://www.dit.ie/timetables/",
		WEBSITE_ADDRESS = ROOT_ADDRESS+"PortalServ",
		LOGIN_ADDRESS = WEBSITE_ADDRESS;

	
	final String logName = "TimetableConnection";

	
	public boolean areCredentialsPresent()
	{
		return TextUtils.isEmpty(settings.getUsername())==false && TextUtils.isEmpty(settings.getPassword())==false;
	}
	
	public Cookie getCookie()
	{
		if (cookie == null) logIn();
		return cookie;
	}
	
	public void logIn()
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
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setSoTimeout(params, HttpUtils.SOCKET_TIMEOUT);
			HttpConnectionParams.setConnectionTimeout(params, HttpUtils.CONNECTION_TIMEOUT);
			httpClient.setParams(params);
			HttpResponse response = httpClient.execute(post);

			if (response.getStatusLine().getStatusCode() < 400)
			{				
				Header h = response.getFirstHeader("Set-Cookie");
				if (h == null) cookie = new Cookie("");
				else cookie = new Cookie(h.getValue());
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
	
	public String getContent(String address) throws IOException
	{
		if (cookie == null || cookie.isValid() == false) logIn();
		String query = WEBSITE_ADDRESS+address;
		Log.d(logName, "Query addr: "+query);		
		
		return HttpUtils.get(query, cookie);
	}
	
	
	@SuppressLint("NewApi")
	public Connection(AppSettings settings)
	{
		this.settings = settings;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
		{
			downloadsFolder = Environment.DIRECTORY_DOWNLOADS;
		}
	}
}
