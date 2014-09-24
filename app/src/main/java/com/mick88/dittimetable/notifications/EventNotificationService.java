package com.mick88.dittimetable.notifications;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;

import java.util.List;

public class EventNotificationService extends Service
{
    private final int NOTIFICATION_ID = 100;

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        TimetableApp app = (TimetableApp) getApplication();
        AppSettings settings = app.getSettings();
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        Timetable timetable = databaseHelper.loadTimetable(settings);
        if (timetable != null)
            showNotification(timetable);
    }

    private NotificationManager getNotificationManager()
    {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    void showNotification(Timetable timetable)
    {
        TimetableDay today = timetable.getToday(true);
        List<TimetableEvent> events = today.getEvents();
        if (events.isEmpty()) return;
        TimetableEvent event = events.get(0);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (TimetableEvent e : events)
            style.addLine(e.toString());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Upcoming event")
                .setContentText(event.toString())
                .setStyle(style);

        getNotificationManager().notify(NOTIFICATION_ID, builder.build());
    }
}
