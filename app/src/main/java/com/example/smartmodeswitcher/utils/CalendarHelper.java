package com.example.smartmodeswitcher.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarHelper {
    private static final String TAG = "CalendarHelper";
    private final Context context;
    private final ContentResolver contentResolver;

    public static class CalendarEvent {
        public final String title;
        public final long startTime;
        public final long endTime;
        public final String location;
        public final String description;
        public final String calendarName;

        public CalendarEvent(String title, long startTime, long endTime, String location, String description, String calendarName) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.description = description;
            this.calendarName = calendarName;
        }
    }

    public CalendarHelper(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public List<CalendarEvent> getTodayEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        
        // Get start and end of today
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        // First, get the Google Calendar ID
        String[] calendarProjection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME
        };

        String calendarSelection = CalendarContract.Calendars.ACCOUNT_NAME + " LIKE ?";
        String[] calendarSelectionArgs = new String[]{"%gmail.com%"};

        try {
            Cursor calendarCursor = contentResolver.query(
                    CalendarContract.Calendars.CONTENT_URI,
                    calendarProjection,
                    calendarSelection,
                    calendarSelectionArgs,
                    null
            );

            if (calendarCursor != null) {
                while (calendarCursor.moveToNext()) {
                    long calendarId = calendarCursor.getLong(0);
                    String calendarName = calendarCursor.getString(1);
                    String accountName = calendarCursor.getString(2);
                    
                    Log.d(TAG, "Found calendar: " + calendarName + " (" + accountName + ")");

                    // Now get events for this calendar
                    String[] eventProjection = new String[]{
                            CalendarContract.Events.TITLE,
                            CalendarContract.Events.DTSTART,
                            CalendarContract.Events.DTEND,
                            CalendarContract.Events.EVENT_LOCATION,
                            CalendarContract.Events.DESCRIPTION
                    };

                    String eventSelection = "(" + CalendarContract.Events.DTSTART + " >= ? AND " +
                            CalendarContract.Events.DTSTART + " <= ?) AND " +
                            CalendarContract.Events.CALENDAR_ID + " = ?";

                    String[] eventSelectionArgs = new String[]{
                            String.valueOf(startOfDay.getTimeInMillis()),
                            String.valueOf(endOfDay.getTimeInMillis()),
                            String.valueOf(calendarId)
                    };

                    Cursor eventCursor = contentResolver.query(
                            CalendarContract.Events.CONTENT_URI,
                            eventProjection,
                            eventSelection,
                            eventSelectionArgs,
                            CalendarContract.Events.DTSTART + " ASC"
                    );

                    if (eventCursor != null) {
                        while (eventCursor.moveToNext()) {
                            String title = eventCursor.getString(0);
                            long startTime = eventCursor.getLong(1);
                            long endTime = eventCursor.getLong(2);
                            String location = eventCursor.getString(3);
                            String description = eventCursor.getString(4);

                            Log.d(TAG, "Found event: " + title + " in calendar " + calendarName);
                            
                            events.add(new CalendarEvent(title, startTime, endTime, location, description, calendarName));
                        }
                        eventCursor.close();
                    }
                }
                calendarCursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading calendar events", e);
        }

        return events;
    }

    public CalendarEvent getCurrentEvent() {
        long currentTime = System.currentTimeMillis();
        List<CalendarEvent> todayEvents = getTodayEvents();

        for (CalendarEvent event : todayEvents) {
            if (currentTime >= event.startTime && currentTime <= event.endTime) {
                Log.d(TAG, "Current event found: " + event.title + " in calendar " + event.calendarName);
                return event;
            }
        }

        Log.d(TAG, "No current events found");
        return null;
    }
} 