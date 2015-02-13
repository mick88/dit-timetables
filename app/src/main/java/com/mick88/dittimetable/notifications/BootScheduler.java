package com.mick88.dittimetable.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mick88.dittimetable.settings.AppSettings;

/**
 * Created by Michal on 07/02/2015.
 */
public class BootScheduler extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            AppSettings settings = new AppSettings(context);
            EventNotificationService.scheduleUpdates(context, settings);
        }
    }
}
