package com.mick88.dittimetable.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventNotificationService extends Service
{
    private final int NOTIFICATION_ID = 100;

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        TimetableApp app = (TimetableApp) getApplication();
        AppSettings settings = app.getSettings();
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        Timetable timetable = databaseHelper.loadTimetable(settings);
        if (timetable != null)
            showNotification(timetable, settings);

        return START_NOT_STICKY;
    }

    private NotificationManagerCompat getNotificationManager()
    {
        return NotificationManagerCompat.from(this);
    }

    int getTargetHour(List<TimetableEvent> events)
    {
        Calendar instance = Calendar.getInstance();
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int min = instance.get(Calendar.MINUTE);
        if (min > 50) hour++;
        hour = 14;

        for (TimetableEvent event : events)
            // keep hour if theres an event at that time
            if (event.getStartHour() == hour)
                return hour;
            // fast forward if there is no lecture
            else if (event.getStartHour() > hour)
                return event.getStartHour();

        return hour;
    }

    void showNotification(Timetable timetable, AppSettings appSettings)
    {
        TimetableDay today = timetable.getDay(1);
        List<TimetableEvent> allEvents = today.getEvents(appSettings);
        final int hour = getTargetHour(allEvents);
        List<TimetableEvent> events = new ArrayList<TimetableEvent>(2);
        for (TimetableEvent event : allEvents)
            if (event.getStartHour() == hour) events.add(event);

        if (events.isEmpty()) return;
        TimetableEvent event = events.get(0);

        String text = String.format(Locale.ENGLISH, "%s %s\n%s",
                event.getStartTime(),
                event.getRoom(),
                event.getName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Upcoming event")
                .setContentInfo(event.getRoom())
                .setContentText(text);

        if (events.size() > 1)
        {
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            style.setBigContentTitle("Upcoming events");
            for (TimetableEvent e : events)
                style.addLine(e.toString());
            builder.setStyle(style);
        }

        getNotificationManager().notify(NOTIFICATION_ID, builder.build());
    }
}
