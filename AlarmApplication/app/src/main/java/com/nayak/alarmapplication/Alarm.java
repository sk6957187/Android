package com.nayak.alarmapplication;

import java.io.Serializable;

public class Alarm implements Serializable {
    private int id;
    private int hours;
    private int minutes;
    private String name;
    private boolean status;


    public Alarm(){}
    public Alarm(int hours, int minutes, String name, boolean status) {
        this.hours = hours;
        this.minutes = minutes;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String hour, minute, format;
        if(hours > 12){
            hour = (hours - 12) + "";
            format = " PM";
        } else if(hours == 0){
            hour = "12";
            format = " AM";
        } else if (hours == 12) {
            hour = "12";
            format = " PM";
        } else {
            hour = hours + "";
            format = " AM";
        }

        if(minutes < 10){
            minute = "0" + minutes;
        } else {
            minute = "" + minutes;
        }

        return hour + ":" + minute + format;
    }
}
