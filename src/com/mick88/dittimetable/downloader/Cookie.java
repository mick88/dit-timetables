package com.mick88.dittimetable.downloader;

import java.io.Serializable;

import android.os.Bundle;
import android.util.Log;

public class Cookie implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String cookie;
	final String logTag = "Cookie";
	
	public Cookie(String cookie)
	{
		this.cookie = cookie;
		Log.d(logTag, "Cookie created: "+cookie);
	}
	
	public boolean isValid()
	{
		return cookie != null;
	}
	
	public Bundle getBundle()
	{
		// JSESSIONID=B61784A1AAF1FD898F4A30904E967F96; Path=/timetables; HttpOnly
		Bundle result = new Bundle();
		String [] pairs = cookie.split(",");
		for (String pair : pairs)
		{
			String [] keyValue = pair.split("=");
			if (keyValue.length == 2) result.putString(keyValue[0].trim(), keyValue[1]);
			else if (keyValue.length == 1) result.putString(keyValue[0].trim(), "");
		}
		return result;
	}
	
	@Override
	public String toString()
	{
		return cookie;
	}
}