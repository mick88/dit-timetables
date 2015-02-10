package com.mick88.dittimetable;

import com.mick88.dittimetable.notifications.EventNotificationService;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;

/**
 * Created by Michal on 10/02/2015.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class EventNotificationTest
{
    @Before
    public void setUp() throws Exception
    {

    }

    @Test
    public void testTargetTimeEvenHour() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        int hour = EventNotificationService.getTargetHour(calendar);

        Assert.assertEquals(10, hour);
    }

    @Test
    public void testTargetTimeBeforeFullHour() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 56);
        int hour = EventNotificationService.getTargetHour(calendar);

        Assert.assertEquals(11, hour);
    }

    @Test
    public void testTargetTimAfterHour() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 10);
        int hour = EventNotificationService.getTargetHour(calendar);

        Assert.assertEquals(10, hour);
    }

    @Test
    public void testTargetTime10minBeforeHour() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 50);
        int hour = EventNotificationService.getTargetHour(calendar);

        Assert.assertEquals(11, hour);
    }
}
