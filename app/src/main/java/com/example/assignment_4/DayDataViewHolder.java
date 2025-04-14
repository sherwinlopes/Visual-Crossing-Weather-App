package com.example.assignment_4;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment_4.databinding.DayItemBinding;

public class DayDataViewHolder extends RecyclerView.ViewHolder {
    private DayItemBinding binding;

    public DayDataViewHolder(DayItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(DayData dayData) {
        binding.dayndate.setText(dayData.getDay());
        binding.highnlow.setText(dayData.getHigh_low());
        binding.dayIcon.setImageResource(dayData.getIcon());
        binding.daydescription.setText(dayData.getDescription());
        binding.preciprob.setText(dayData.getPrecipitation());
        binding.dayUv.setText(dayData.getUV_index());
        binding.mornTemp.setText(String.valueOf(dayData.getMorning_temp()));
        binding.afterTemp.setText(String.valueOf(dayData.getAfternoon_temp()));
        binding.evenTemp.setText(String.valueOf(dayData.getEvening_temp()));
        binding.nightTemp.setText(String.valueOf(dayData.getNight_temp()));

        Log.d("DayDataViewHolder", "bind: " + dayData.getHigh_low());

        String highLow = dayData.getHigh_low();

        String[] parts = highLow.split("/");

        String highTemp = parts[0].trim();

        String highTempValue = highTemp.replaceAll("[^\\d.]", "");
        String highUnit = highTemp.replaceAll("[^a-zA-Z]", "");

        double highTempDouble = Double.parseDouble(highTempValue);
        ColorMaker.setColorGradient(binding.getRoot(), highTempDouble, highUnit);
    }
}
