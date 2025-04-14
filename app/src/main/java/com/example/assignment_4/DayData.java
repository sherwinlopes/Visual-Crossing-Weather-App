package com.example.assignment_4;

public class DayData {
    private String day;
    private String max_temp;
    private String min_temp;
    private int icon;
    private String description;
    private String precipitation;
    private String uv_index;
    private String morning_temp;
    private String afternoon_temp;
    private String evening_temp;
    private String night_temp;

    public DayData(String day, String max_temp, String min_temp , int icon, String description, String precipitation, String uv_index, String morning_temp, String afternoon_temp, String evening_temp, String night_temp) {
        this.day = day;
        this.max_temp = max_temp;
        this.min_temp = min_temp;
        this.icon = icon;
        this.description = description;
        this.precipitation = precipitation;
        this.uv_index = uv_index;
        this.morning_temp = morning_temp;
        this.afternoon_temp = afternoon_temp;
        this.evening_temp = evening_temp;
        this.night_temp = night_temp;
    }

    public String getDay() {
        return day;
    }

    public String getMax_temp() {
        return max_temp;
    }

    public String getHigh_low() {
        return max_temp + "/" + min_temp;
    }

    public int getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public String getUV_index() {
        return uv_index;
    }

    public String getMorning_temp() {
        return morning_temp;
    }

    public String getAfternoon_temp() {
        return afternoon_temp;
    }

    public String getEvening_temp() {
        return evening_temp;
    }

    public String getNight_temp() {
        return night_temp;
    }

}
