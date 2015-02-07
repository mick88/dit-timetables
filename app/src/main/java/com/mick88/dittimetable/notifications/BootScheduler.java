package com.mick88.dittimetable.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
            EventNotificationService.scheduleUpdates(context);
        }
    }
}
