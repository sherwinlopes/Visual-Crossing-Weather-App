package com.example.assignment_4;

public class HourlyWeather {
    private String day;
    private String time;
    private int icon;
    private String hour_temp;
    private String hour_condition;

    public HourlyWeather(String day, String time, int icon, String hour_temp, String hour_condition) {
        this.day = day;
        this.time = time;
        this.icon = icon;
        this.hour_temp = hour_temp;
        this.hour_condition = hour_condition;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public int getIcon() {
        return icon;
    }

    public String getHour_temp() {
        return hour_temp;
    }

    public String getHour_condition() {
        return hour_condition;
    }
}
