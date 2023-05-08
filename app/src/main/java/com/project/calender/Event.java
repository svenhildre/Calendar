package com.project.calender;

public class Event {
    private String name;
    private String time;
    private String description;
    private int year;
    private int month;
    private int dayOfMonth;

    public Event() {}

    public Event(String name, String time, String description, int year, int month, int dayOfMonth) {
        this.name = name;
        this.time = time;
        this.description = description;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
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
}