package com.example.assignment_4;

import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment_4.databinding.HourlyWeatherBinding;

public class HourlyWeatherViewHolder extends RecyclerView.ViewHolder {
    private HourlyWeatherBinding binding;

    public HourlyWeatherViewHolder(HourlyWeatherBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(HourlyWeather hourlyWeather) {
        binding.today.setText(hourlyWeather.getDay());
        binding.oneHour.setText(hourlyWeather.getTime());
        binding.hourIcon.setImageResource(hourlyWeather.getIcon());
        binding.hourTemp.setText(hourlyWeather.getHour_temp());
        binding.hourCondition.setText(hourlyWeather.getHour_condition());
    }
}
