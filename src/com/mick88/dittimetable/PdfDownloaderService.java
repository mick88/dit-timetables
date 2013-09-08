package com.mick88.dittimetable;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PdfDownloaderService extends Service
{
	public PdfDownloaderService() {
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
