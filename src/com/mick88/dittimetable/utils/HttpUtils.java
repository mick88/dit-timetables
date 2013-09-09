package com.mick88.dittimetable.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.mick88.dittimetable.web.Cookie;

public class HttpUtils
{
	public static String get(String query, Cookie cookie) throws IOException
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setSoTimeout(params, 10000);
		HttpConnectionParams.setConnectionTimeout(params, 10000);
		httpClient.setParams(params);
		
		HttpGet get = new HttpGet(query);
		get.setHeader("Cookie", cookie.toString());
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
	
}
