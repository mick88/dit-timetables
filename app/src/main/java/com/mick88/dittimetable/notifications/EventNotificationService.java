package com.mick88.dittimetable.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.mick88.dittimetable.DatabaseHelper;
import com.mick88.dittimetable.R;
import com.mick88.dittimetable.TimetableApp;
import com.mick88.dittimetable.settings.AppSettings;
import com.mick88.dittimetable.timetable.Timetable;
import com.mick88.dittimetable.timetable.TimetableDay;
import com.mick88.dittimetable.timetable.TimetableEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventNotificationService extends Service
{
    private static final String
        NOTIFICATION_GROUP = "upcoming_events",
        NOTIFICATION_TAG = "upcoming_event";
    private static final int NOTIFICATION_ID = 100;
    private static final int
        ALARM_REQUEST_CODE = 120,
        HANDICAP_MIN = 10;

    String courseCode;

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
        if (settings.getEventNotifications())
        {
            DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
            Timetable timetable = databaseHelper.loadTimetable(settings);
            if (timetable != null)
                showNotification(timetable, settings);
        }

        return START_NOT_STICKY;
    }

    private NotificationManagerCompat getNotificationManager()
    {
        return NotificationManagerCompat.from(this);
    }

    public static int getTargetHour(Calendar time)
    {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int min = time.get(Calendar.MINUTE);
        if (min >= (60-HANDICAP_MIN)) hour++;

        return hour;
    }

    void showNotification(Timetable timetable, AppSettings appSettings)
    {
        NotificationManagerCompat notificationManager = getNotificationManager();
        // cancel previous notifications
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);

        this.courseCode = appSettings.getCourse();
        TimetableDay today = timetable.getToday(false);
        if (today == null) return;
        List<TimetableEvent> allEvents = today.getEvents(appSettings);
        final int hour = getTargetHour(Calendar.getInstance());

        // make sure user isnt notified twice about the same events:
        if (appSettings.wasTimeslotNotified(today.getId(), hour))
            return;

        List<TimetableEvent> events = new ArrayList<TimetableEvent>(2);
        for (TimetableEvent event : allEvents)
            if (event.getStartHour() == hour) events.add(event);

        if (events.isEmpty())
        {
            clearNotification(getApplicationContext());
            return;
        }

        // post new notifications
        notifyEvents(notificationManager, events, hour);
    }

    public static void clearNotification(Context context)
    {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.cancel(NOTIFICATION_ID);
    }

    void notifyEvents(NotificationManagerCompat notificationManager, List<TimetableEvent> events, int hour)
    {
        for (TimetableEvent event : events)
            postEventNotification(notificationManager, event, events.indexOf(event)+1);

        postSummaryNotification(notificationManager, events, hour);
    }

    void postSummaryNotification(NotificationManagerCompat manager, List<TimetableEvent> events, int hour)
    {
        if (events.size() >= 1)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, events.get(0).getStartMin());

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getEventRooms(events))
                    .setContentText(getEventNames(events))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setWhen(calendar.getTimeInMillis())
                    .setGroup(NOTIFICATION_GROUP)
                    .setContentInfo(events.get(0).getType().toString())
                    .setGroupSummary(true);

            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
            style.setBigContentTitle("Upcoming events");
            String name = null;
            for (TimetableEvent event : events)
            {
                if (event.getName().equals(name) == false)
                {
                    name = event.getName();
                    style.addLine(name);
                }
                StringBuilder eventBuilder = new StringBuilder()
                        .append(event.getRoom())
                        .append(" (")
                        .append(getGroupString(event))
                        .append(")");
                style.addLine(eventBuilder);
            }
            builder.setStyle(style);

            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.kevinst);
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                    .setBackground(drawable.getBitmap());
            builder.extend(extender);

            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    static CharSequence getEventNames(Collection<TimetableEvent> events)
    {
        Set<String> names = new HashSet<String>(1);
        for (TimetableEvent event : events)
            names.add(event.getName());
        String separator = "";
        StringBuilder nameBuilder = new StringBuilder();
        for (String name : names)
        {
            nameBuilder.append(separator).append(name);
            separator = ", ";
        }

        return nameBuilder;
    }

    static CharSequence getEventRooms(Collection<TimetableEvent> events)
    {
        Set<String> rooms = new HashSet<String>(1);
        for (TimetableEvent event : events)
            rooms.add(event.getRoom());
        String separator = "";
        StringBuilder roomBuilder = new StringBuilder();
        for (String room : rooms)
        {
            roomBuilder.append(separator).append(room);
            separator = ", ";
        }

        return roomBuilder;
    }

    NotificationCompat.Builder buildEventNotification(TimetableEvent event)
    {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder()
                .append(event.getStartTime())
                .append(' ')
                .append(getGroupString(event))
                .append('\n')
                .append(event.getName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(event.getRoom())
                .setContentInfo(event.getEventTimeString())
                .setGroup(NOTIFICATION_GROUP)
                .setSortKey(event.getGroupStr())
                .setContentText(spannableStringBuilder);

        return builder;
    }

    CharSequence getGroupString(TimetableEvent event)
    {
        String groupStr = event.getGroupsString(courseCode);
        if (TextUtils.isEmpty(groupStr))
            return "";
        SpannableString string = new SpannableString(groupStr);
        int color = getResources().getColor(R.color.color_group);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        string.setSpan(colorSpan, 0, string.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return string;
    }

    void postEventNotification(NotificationManagerCompat notificationManager, TimetableEvent event, int notificationIdOffset)
    {
        NotificationCompat.Builder builder = buildEventNotification(event);

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID + notificationIdOffset, builder.build());
    }

    protected static PendingIntent getPendingIntent(Context context)
    {
        Intent intent = new Intent(context, EventNotificationService.class);
        return PendingIntent.getService(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleUpdates(Context context, AppSettings settings)
    {
        if (settings.getEventNotifications() == false) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, (60-HANDICAP_MIN));
        calendar.set(Calendar.SECOND, 0);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = getPendingIntent(context);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000*60*60, pendingIntent);
    }

    public static void cancelScheduledUpdates(Context context)
    {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        final PendingIntent pendingIntent = getPendingIntent(context);

        alarmManager.cancel(pendingIntent);
    }

}
