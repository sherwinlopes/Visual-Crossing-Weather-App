package com.example.assignment_4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.assignment_4.databinding.ActivityDayBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayActivity extends AppCompatActivity {

    private static final String API_KEY = "FUFYSM9S2EPWSSCVL6ADJ3852";
    private ActivityDayBinding binding;
    private DayDataAdapter adapter;
    private final List<DayData> dayDataList = new ArrayList<>();

    public static int getId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String currentLocation = intent.getStringExtra("location");
        String unit = intent.getStringExtra("unit");


        binding = ActivityDayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new DayDataAdapter(dayDataList);
        binding.dayRecycle.setLayoutManager(new LinearLayoutManager(this));
        binding.dayRecycle.setAdapter(adapter);

        fetchDayData(currentLocation, unit);
        binding.dayLocation.setText(currentLocation + " 15-Day Forecast");


        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchDayData(String location, String unit) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("weather.visualcrossing.com")
                .appendPath("VisualCrossingWebServices")
                .appendPath("rest")
                .appendPath("services")
                .appendPath("timeline")
                .appendPath(location)
                .appendQueryParameter("unitGroup", unit)
                .appendQueryParameter("key", API_KEY);

        String url = builder.build().toString();
        Log.d("WeatherApp", "Request URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray daysArray = response.getJSONArray("days");

                        for (int i = 0; i < daysArray.length(); i++) {
                            JSONObject day = daysArray.getJSONObject(i);

                            long datetimeEpoch = day.getLong("datetimeEpoch");
                            String date = convertEpochToDate(datetimeEpoch);

                            String newUnit;

                            if (unit.equals("us")) {
                                newUnit = "°F";
                            } else {
                                newUnit = "°C";
                            }
                            double maxTempDouble = day.getDouble("tempmax");
                            int maxTempInt = (int) Math.round(maxTempDouble);
                            double minTempDouble = day.getDouble("tempmin");
                            int minTempInt = (int) Math.round(minTempDouble);

                            String maxtempText = String.format(Locale.getDefault(), "%d%s", maxTempInt, newUnit);
                            String mintempText = String.format(Locale.getDefault(), "%d%s", minTempInt, newUnit);

                            String icon = day.getString("icon");
                            int iconID = getWeatherIconId(icon);

                            String description = day.getString("description");

                            double precipProbabilityDouble = day.getDouble("precipprob");
                            int precipProbability = (int) Math.round(precipProbabilityDouble);
                            String precipProbabilityText = "(" + precipProbability + "% precip.)";

                            double uvIndexDouble = day.getDouble("uvindex");
                            int uvIndex = (int) Math.round(uvIndexDouble);

                            String uvIndexText = "UV Index: " + uvIndex;

                            JSONArray hoursArray = day.getJSONArray("hours");

                            double mornTempDouble = hoursArray.getJSONObject(8).getDouble("temp");
                            int mornTempInt = (int) Math.round(mornTempDouble);

                            double afterTempDouble = hoursArray.getJSONObject(12).getDouble("temp");
                            int afterTempInt = (int) Math.round(afterTempDouble);

                            double evenTempDouble = hoursArray.getJSONObject(16).getDouble("temp");
                            int evenTempInt = (int) Math.round(evenTempDouble);

                            double nightTempDouble = hoursArray.getJSONObject(20).getDouble("temp");
                            int nightTempInt = (int) Math.round(nightTempDouble);

                            String mornTempText = String.format(Locale.getDefault(), "%d%s", mornTempInt, newUnit);
                            String afterTempText = String.format(Locale.getDefault(), "%d%s", afterTempInt, newUnit);
                            String evenTempText = String.format(Locale.getDefault(), "%d%s", evenTempInt, newUnit);
                            String nightTempText = String.format(Locale.getDefault(), "%d%s", nightTempInt, newUnit);

                            DayData dayData = new DayData(date, maxtempText, mintempText, iconID, description, precipProbabilityText, uvIndexText, mornTempText, afterTempText, evenTempText, nightTempText);
                            dayDataList.add(dayData);
                        }

                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("WeatherApp", "Failed to retrieve data: " + error.getMessage());
                    Toast.makeText(DayActivity.this, "Failed to get weather data", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private String convertEpochToDate(long epochTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MM/d", Locale.getDefault());
        Date date = new Date(epochTime * 1000);
        return dateFormat.format(date);
    }

    private int getWeatherIconId(String icon) {
        icon = icon.replace("-", "_");
        int iconID = getId(icon, R.drawable.class);
        if (iconID == 0) {
            iconID = R.mipmap.ic_launcher;
        }
        return iconID;
    }

}