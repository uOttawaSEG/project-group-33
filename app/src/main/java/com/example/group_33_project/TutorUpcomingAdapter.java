package com.example.group_33_project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZonedDateTime;
import java.util.List;

public class TutorUpcomingAdapter extends RecyclerView.Adapter<TutorUpcomingAdapter.TimeSlotViewHolder> {

    private List<SimpleTutorSlot> timeSlots;

    public TutorUpcomingAdapter(List<SimpleTutorSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simpleslot_tutorslot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        SimpleTutorSlot slot = timeSlots.get(position);

        // Determine if slot is in the past
        ZonedDateTime now = ZonedDateTime.now();
        boolean isPast = slot.start.isBefore(now);

        // Set the label
        if ("booked".equals(slot.status)) {
            holder.tvSlot.setText(slot.name);
        } else {
            holder.tvSlot.setText("");
        }

        // Background color depending on slot state
        if (isPast) {
            holder.tvSlot.setBackgroundColor(Color.GRAY);
        } else {
            switch (slot.status) {
                case "booked":
                        holder.tvSlot.setBackgroundColor(Color.BLUE);
                    break;
                default:
                    holder.tvSlot.setBackgroundColor(Color.LTGRAY); // empty or default
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlot;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlot = itemView.findViewById(R.id.tvSlot);
        }
    }
}
