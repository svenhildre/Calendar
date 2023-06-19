package com.project.calender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String eventId;
    private String name;
    private String time;
    private String description;
    private int year;
    private int month;
    private int dayOfMonth;
    private boolean isReminderEnabled;
    private int selectedReminder;

    public Event() {
        // Boş yapıcı yöntem gereklidir
    }

    public Event(String eventId, String name, String time, String description, int year, int month, int dayOfMonth) {
        this.eventId = eventId;
        this.name = name;
        this.time = time;
        this.description = description;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isReminderEnabled() {
        return isReminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        isReminderEnabled = reminderEnabled;
    }

    public int getSelectedReminder() {
        return selectedReminder;
    }

    public void setSelectedReminder(int selectedReminder) {
        this.selectedReminder = selectedReminder;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public long getEventTimeInMillis() {
        // Etkinlik zamanını milisaniye cinsinden hesapla
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date eventDate = null;
        try {
            eventDate = timeFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (eventDate != null) {
            calendar.set(Calendar.HOUR_OF_DAY, eventDate.getHours());
            calendar.set(Calendar.MINUTE, eventDate.getMinutes());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar.getTimeInMillis();
    }
}
