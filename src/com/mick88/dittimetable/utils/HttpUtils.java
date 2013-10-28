package com.mick88.dittimetable.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.mick88.dittimetable.web.Connection;
import com.mick88.dittimetable.web.Cookie;

public class HttpUtils
{
	public static class ConnectionException extends IOException
	{
		public ConnectionException(String message)
		{
			super(message);
		}
	}
	
	public static class ConnectionResponseException extends ConnectionException
	{
		final int statusCode;
		public ConnectionResponseException(String message, int statusCode)
		{
			super(message);
			this.statusCode = statusCode;
		}
		
		public int getStatusCode()
		{
			return statusCode;
		}
	}
	
	public static final int 
		SOCKET_TIMEOUT = 20000,
		CONNECTION_TIMEOUT = 10000;

	public static String get(String query, Cookie cookie) throws IOException
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		httpClient.setParams(params);
		
		HttpGet get = new HttpGet(query);
		get.setHeader("Cookie", cookie.toString());
		get.setHeader("User-Agent", Connection.USER_AGENT);
		HttpResponse response = httpClient.execute(get);
		
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() / 100 != 2)
			throw new ConnectionResponseException(status.getReasonPhrase(), status.getStatusCode());
		else if (response.getEntity().getContentLength() == 0l)
			throw new ConnectionException("Server returned empty result");
		
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
	
}
