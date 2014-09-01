package com.mick88.dittimetable.utils;

import android.content.Context;

import java.io.FileInputStream;

public class FileUtils
{
	public static String readFile(Context context, String filename)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			if (context.getFileStreamPath(filename).exists() == false)
			{
				return null;
			}
			
			final int BUFFER_SIZE = 30000;
			FileInputStream f = context.openFileInput(filename);
			
			byte[] buffer = new byte[BUFFER_SIZE];	
			
			while (f.read(buffer) > 0)
			{
				String line = new String(buffer);
				sb.append(line);				
				buffer = new byte[BUFFER_SIZE];
			}
			
			f.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
	}
}
