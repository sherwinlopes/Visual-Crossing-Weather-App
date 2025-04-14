package com.example.assignment_4;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.assignment_4.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "FUFYSM9S2EPWSSCVL6ADJ3852";
    private static final String FAHRENHEIT_TAG = "fahrenheit";
    private static final String CELSIUS_TAG = "celsius";
    private static final int LOCATION_REQUEST = 111;
    private ActivityMainBinding binding;
    private final List<HourlyWeather> hourlyWeatherList = new ArrayList<>();
    private HourlyWeatherAdapter adapter;
    private String currentLocation;
    private FusedLocationProviderClient mFusedLocationClient;

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


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new HourlyWeatherAdapter(hourlyWeatherList);
        binding.hourlyRecycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.hourlyRecycle.setAdapter(adapter);

        initializeUnitIcon();

        binding.targetIcon.setOnClickListener(v -> resetLocation());
        binding.unitIcon.setOnClickListener(v -> toggleUnit());
        binding.locationIcon.setOnClickListener(v -> showLocationDialog());
        binding.calIcon.setOnClickListener(v -> DayActivity());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.main.setOnRefreshListener(() -> {
            if(currentLocation != null) {
                fetchTemperature(currentLocation);
            } else {
                fetchLocation();
            }
        });
    }

    private TreeMap<String, Double> makeTemperaturePointsFromJSON(String jsonData) {
        TreeMap<String, Double> timeTempValues = new TreeMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray daysArray = jsonObject.getJSONArray("days");

            if (daysArray.length() > 0) {
                JSONObject firstDay = daysArray.getJSONObject(0);
                JSONArray hoursArray = firstDay.getJSONArray("hours");

                for (int i = 0; i < hoursArray.length(); i++) {
                    JSONObject hourData = hoursArray.getJSONObject(i);
                    String timeStr = hourData.getString("datetime");
                    double temp = hourData.getDouble("temp");
                    timeTempValues.put(timeStr, temp);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return timeTempValues;
    }

    public void displayChartTemp(float time, float tempVal) {
        SimpleDateFormat sdf =
                new SimpleDateFormat("h a", Locale.US);
        Date d = new Date((long) time);
        binding.chartTemp.setText(
                String.format(Locale.getDefault(),
                        "%s, %.0f°",
                        sdf.format(d), tempVal));
        binding.chartTemp.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                runOnUiThread(() -> binding.chartTemp.setVisibility(View.GONE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void resetLocation() {
        fetchLocation();
    }

    private void initializeUnitIcon() {
        binding.unitIcon.setImageResource(R.drawable.units_f);
        binding.unitIcon.setTag(FAHRENHEIT_TAG);
    }

    private void toggleUnit() {
        if (binding.unitIcon.getTag() != null && binding.unitIcon.getTag().equals(FAHRENHEIT_TAG)) {
            binding.unitIcon.setImageResource(R.drawable.units_c);
            binding.unitIcon.setTag(CELSIUS_TAG);
        } else {
            binding.unitIcon.setImageResource(R.drawable.units_f);
            binding.unitIcon.setTag(FAHRENHEIT_TAG);
        }
        fetchTemperature(currentLocation);
    }

    private void showLocationDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_box, null);
        EditText input = dialogView.findViewById(R.id.location_input);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a Location")
                .setMessage("For US Locations, please enter as 'City' or 'City, State'. For International Locations, please enter as 'City, Country'.")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, id) -> {
                    String userInput = input.getText().toString();
                    currentLocation = userInput;
                    fetchTemperature(currentLocation);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void fetchLocation() {
        if (!isNetworkConnected()) {
            showNoInternetConnectionDialog();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String locationString = latitude + "," + longitude;
                        fetchTemperature(locationString);
                    } else {
                        showLocationErrorDialog("Unknown Location");
                    }
                })
                .addOnFailureListener(this, e -> {
                    showLocationErrorDialog("Unknown Location");

                });
    }

    private void fetchTemperature(String location) {

        if (!isNetworkConnected()) {
            showNoInternetConnectionDialog();
            return;
        }

        String unit = binding.unitIcon.getTag().equals(FAHRENHEIT_TAG) ? "us" : "metric";

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
                        JSONObject currentConditions = response.getJSONObject("currentConditions");

                        TreeMap<String, Double> tempPoints = makeTemperaturePointsFromJSON(response.toString());
                        ChartMaker chartMaker = new ChartMaker(this, binding);
                        chartMaker.makeChart(tempPoints, System.currentTimeMillis());
                        String resolvedAddress = response.optString("resolvedAddress");

                        String cityName = extractCityName(resolvedAddress);

                        String timezone = response.getString("timezone");
                        double temperature = currentConditions.getDouble("temp");
                        String icon = currentConditions.getString("icon");
                        double feelsLike = currentConditions.getDouble("feelslike");
                        String condition = currentConditions.getString("conditions");
                        double cloudCover = currentConditions.getDouble("cloudcover");
                        double humidity = currentConditions.getDouble("humidity");
                        double uvIndex = currentConditions.getDouble("uvindex");
                        double visibility = currentConditions.getDouble("visibility");

                        long sunriseEpoch = currentConditions.getLong("sunriseEpoch");
                        String sunriseTime = formatEpochTime(sunriseEpoch, timezone);

                        long sunsetEpoch = currentConditions.getLong("sunsetEpoch");
                        String sunsetTime = formatEpochTime(sunsetEpoch, timezone);

                        double windSpeed = currentConditions.getDouble("windspeed");
                        int windspeedRound = (int) Math.round(windSpeed);
                        int windDir = currentConditions.getInt("winddir");
                        String windDirText = getDirection(windDir);

                        String windGustText = "";
                        if (currentConditions.has("windgust") && !currentConditions.isNull("windgust")) {
                            double windGust = currentConditions.getDouble("windgust");
                            int windGustRound = (int) Math.round(windGust);
                            windGustText = String.format(", gusting to %d mph", windGustRound);
                        }

                        String windText = String.format("Winds: %s at %d mph%s", windDirText, windspeedRound, windGustText);
                        binding.wind.setText(windText);

                        String unitSymbol = binding.unitIcon.getTag().equals(FAHRENHEIT_TAG) ? "°F" : "°C";
                        String locationText = String.format("%s, %s", cityName, getCurrentDate(timezone));
                        int tempRounded = (int) Math.round(temperature);
                        String tempText = String.format("%d %s", tempRounded, unitSymbol);
                        int iconID = getWeatherIconId(icon);
                        int feelsLikeRounded = (int) Math.round(feelsLike);
                        String feelsLikeText = String.format("Feels like %d %s", feelsLikeRounded, unitSymbol);
                        int cloudCoverRounded = (int) Math.round(cloudCover);
                        String cloudCoverText = String.format("(%d%% clouds)", cloudCoverRounded);
                        String conditionText = String.format("%s %s", condition, cloudCoverText);
                        int humidityRounded = (int) Math.round(humidity);
                        String humidityText = String.format("Humidity: %d%%", humidityRounded);
                        int uvIndexRounded = (int) Math.round(uvIndex);
                        String uvIndexText = String.format("UV Index: %d", uvIndexRounded);
                        String visibilityText = String.format("Visibility: %.1f miles", visibility);
                        String sunriseText = String.format("Sunrise: %s", sunriseTime);
                        String sunsetText = String.format("Sunset: %s", sunsetTime);

                        String day = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());

                        binding.cityDate.setText(locationText);
                        binding.degUnit.setText(tempText);
                        binding.weatherIcon.setImageResource(iconID);
                        binding.feelsLike.setText(feelsLikeText);
                        binding.condition.setText(conditionText);
                        binding.humidity.setText(humidityText);
                        binding.uxIndex.setText(uvIndexText);
                        binding.visibility.setText(visibilityText);
                        binding.sunrise.setText(sunriseText);
                        binding.sunset.setText(sunsetText);

                        String unitLetter = binding.unitIcon.getTag().equals(FAHRENHEIT_TAG) ? "F" : "C";
                        ColorMaker.setColorGradient(binding.main, temperature, unitLetter);
                        ColorMaker.setColorGradient(binding.constraintLayout, temperature, unitLetter);
                        ColorMaker.setColorGradient(binding.hourlyRecycle, temperature, unitLetter);

                        weatherData(response);
                        TreeMap<String, Double> hourlyTemps = getHourlyTemps(response);

                        binding.mapIcon.setOnClickListener(v -> clickMap(resolvedAddress));

                        binding.shareIcon.setOnClickListener(v -> shareWeatherData(response));

                        binding.main.setRefreshing(false);

                        binding.progressBar.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showWeatherDataErrorDialog();
                    }
                },
                error -> {
                    Log.e("WeatherApp", "Failed to retrieve data: " + error.getMessage());
                    showWeatherDataErrorDialog();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void weatherData(JSONObject response) {
        hourlyWeatherList.clear();

        try {
            JSONArray daysArray = response.getJSONArray("days");
            long currentTime = System.currentTimeMillis() / 1000;

            for (int i = 0; i < daysArray.length(); i++) {
                JSONObject dayObject = daysArray.getJSONObject(i);
                JSONArray hoursArray = dayObject.getJSONArray("hours");

                for (int j = 0; j < hoursArray.length(); j++) {
                    JSONObject hourObject = hoursArray.getJSONObject(j);
                    long datetimeEpoch = hourObject.getLong("datetimeEpoch");

                    if (datetimeEpoch >= currentTime) {
                        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                .format(new Date(datetimeEpoch * 1000));

                        String day = (i == 0) ? "Today" : new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date(datetimeEpoch * 1000));

                        String icon = hourObject.getString("icon");
                        int iconID = getWeatherIconId(icon);

                        String unitSymbol = binding.unitIcon.getTag().equals(FAHRENHEIT_TAG) ? "°F" : "°C";
                        double temp = hourObject.getDouble("temp");
                        int tempRounded = (int) Math.round(temp);
                        String tempText = String.format("%d %s", tempRounded, unitSymbol);


                        String conditions = hourObject.getString("conditions");

                        hourlyWeatherList.add(new HourlyWeather(day, time, iconID, tempText, conditions));
                    }
                }
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private int getWeatherIconId(String icon) {
        icon = icon.replace("-", "_");
        int iconID = getId(icon, R.drawable.class);
        if (iconID == 0) {
            iconID = R.mipmap.ic_launcher;
        }
        return iconID;
    }

    private String extractCityName(String resolvedAddress) {
        if (resolvedAddress.matches("-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?")) {
            return geoCodeLatLong(resolvedAddress);
        } else {
            String[] parts = resolvedAddress.split(",");
            return parts[0].trim();
        }
    }

    private String geoCodeLatLong(String latLong) {
        String[] coordinates = latLong.split(",");
        double latitude = Double.parseDouble(coordinates[0].trim());
        double longitude = Double.parseDouble(coordinates[1].trim());

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return latitude + ", " + longitude;
    }


    private String getDirection(double degrees) {
        if (degrees >= 337.5 || degrees < 22.5)
            return "N";
        if (degrees >= 22.5 && degrees < 67.5)
            return "NE";
        if (degrees >= 67.5 && degrees < 112.5)
            return "E";
        if (degrees >= 112.5 && degrees < 157.5)
            return "SE";
        if (degrees >= 157.5 && degrees < 202.5)
            return "S";
        if (degrees >= 202.5 && degrees < 247.5)
            return "SW";
        if (degrees >= 247.5 && degrees < 292.5)
            return "W";
        return "NW";
    }

    private String getCurrentDate(String timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        return sdf.format(new Date());
    }

    private String formatEpochTime(long epochTime, String timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        return sdf.format(new Date(epochTime * 1000));
    }

    public TreeMap<String, Double> getHourlyTemps(JSONObject response) {
        TreeMap<String, Double> hourlyTemps = new TreeMap<>();
        try {
            JSONArray daysArray = response.getJSONArray("days");

            for (int i = 0; i < daysArray.length(); i++) {
                JSONArray hoursArray = daysArray.getJSONObject(i).getJSONArray("hours");

                for (int j = 0; j < hoursArray.length(); j++) {
                    JSONObject hourObject = hoursArray.getJSONObject(j);
                    long datetimeEpoch = hourObject.getLong("datetimeEpoch");
                    String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(datetimeEpoch * 1000));
                    double temp = hourObject.getDouble("temp");
                    hourlyTemps.put(time, temp);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
        return hourlyTemps;
    }

    private void DayActivity() {
        Intent intent = new Intent(this, DayActivity.class);
        intent.putExtra("location", currentLocation);
        intent.putExtra("unit", binding.unitIcon.getTag().equals(FAHRENHEIT_TAG) ? "us" : "metric");
        startActivity(intent);
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.alert)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLocationErrorDialog(String location) {
        String title = "Location Error";
        String message = "The specified location '" + location + "' could not be resolved. Please try a different location.";
        showErrorDialog(title, message);
    }

    private void showWeatherDataErrorDialog() {
        String title = "Weather Data Error";
        String message = "There was an error retrieving the weather data. Please try again later.";
        showErrorDialog(title, message);
    }

    private void showNoInternetConnectionDialog() {
        String title = "No Internet Connection";
        String message = "This app requires an internet connection to function properly. Please check your connection and try again.";
        showErrorDialog(title, message);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void shareWeatherData(JSONObject response) {
        try {
            JSONObject currentConditions = response.getJSONObject("currentConditions");

            String resolvedAddress = response.optString("resolvedAddress");
            if (resolvedAddress == null || resolvedAddress.isEmpty()) {
                showLocationErrorDialog("Unknown Location");
                return;
            }
            String cityName = extractCityName(resolvedAddress);

            String timezone = response.getString("timezone");
            double temperature = currentConditions.getDouble("temp");
            String icon = currentConditions.getString("icon");
            double feelsLike = currentConditions.getDouble("feelslike");
            String condition = currentConditions.getString("conditions");
            double cloudCover = currentConditions.getDouble("cloudcover");
            double humidity = currentConditions.getDouble("humidity");
            double uvIndex = currentConditions.getDouble("uvindex");
            double visibility = currentConditions.getDouble("visibility");

            long sunriseEpoch = currentConditions.getLong("sunriseEpoch");
            String sunriseTime = formatEpochTime(sunriseEpoch, timezone);

            long sunsetEpoch = currentConditions.getLong("sunsetEpoch");
            String sunsetTime = formatEpochTime(sunsetEpoch, timezone);

            double windSpeed = currentConditions.getDouble("windspeed");
            int windspeedRound = (int) Math.round(windSpeed);
            int windDir = currentConditions.getInt("winddir");
            String windDirText = getDirection(windDir);

            String unitSymbol = binding.unitIcon.getTag().equals(FAHRENHEIT_TAG) ? "°F" : "°C";
            String locationText = String.format("%s, %s", cityName, getCurrentDate(timezone));
            int tempRounded = (int) Math.round(temperature);
            String tempText = String.format("%d %s", tempRounded, unitSymbol);
            int feelsLikeRounded = (int) Math.round(feelsLike);
            String feelsLikeText = String.format("Feels like %d %s", feelsLikeRounded, unitSymbol);
            int cloudCoverRounded = (int) Math.round(cloudCover);
            String cloudCoverText = String.format("(%d%% clouds)", cloudCoverRounded);
            String conditionText = String.format("%s %s", condition, cloudCoverText);
            int humidityRounded = (int) Math.round(humidity);
            String humidityText = String.format("Humidity: %d%%", humidityRounded);
            int uvIndexRounded = (int) Math.round(uvIndex);
            String uvIndexText = String.format("UV Index: %d", uvIndexRounded);
            String visibilityText = String.format("Visibility: %.1f miles", visibility);
            String sunriseText = String.format("Sunrise: %s", sunriseTime);
            String sunsetText = String.format("Sunset: %s", sunsetTime);

            String shareMessage = String.format(
                    "Weather for %s:\n" +
                            "Forecast: %s with a high of %s and a low of %s.\n" +
                            "Now: %s %s (Feels like: %s)\n" +
                            "Humidity: %d%%\n" +
                            "Winds: %s at %d mph\n" +
                            "UV Index: %d\n" +
                            "Sunrise: %s\n" +
                            "Sunset: %s\n" +
                            "Visibility: %.1f miles",
                    locationText,
                    conditionText,
                    tempText,
                    tempText,
                    tempText,
                    condition,
                    feelsLikeText,
                    humidityRounded,
                    windDirText,
                    windspeedRound,
                    uvIndexRounded,
                    sunriseText,
                    sunsetText,
                    visibility
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Weather for " + cityName);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            startActivity(Intent.createChooser(shareIntent, "Share Weather Info"));

        } catch (JSONException e) {
            e.printStackTrace();
            showWeatherDataErrorDialog();
        }
    }

    public void clickMap(String resolvedAddress) {
        if (resolvedAddress == null || resolvedAddress.isEmpty()) {
            showLocationErrorDialog("Unknown Location");
            return;
        }
        Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(resolvedAddress));

        Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showErrorDialog("Map Error", "No app can handle the map request.");
        }
    }
}
