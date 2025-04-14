package com.example.assignment_4;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment_4.databinding.DayItemBinding;

import java.util.List;

public class DayDataAdapter extends RecyclerView.Adapter<DayDataViewHolder> {
    private DayItemBinding binding;

    private final List<DayData> dayDataList;

    public DayDataAdapter(List<DayData> dayDataList) {
        this.dayDataList = dayDataList;
    }

    @Override
    public DayDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = DayItemBinding.inflate(inflater, parent, false);
        return new DayDataViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayDataViewHolder holder, int position) {
        holder.bind(dayDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dayDataList.size();
    }
}
