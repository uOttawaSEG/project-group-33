package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZonedDateTime;
import java.util.List;

public class StudentLookupSessionAdapter extends RecyclerView.Adapter<StudentLookupSessionAdapter.TimeSlotViewHolder> {

    private final List<TimeSlot> timeSlotList;
    private final OnRequestClickListener requestClickListener;

    // Callback when REQUEST button is pressed
    public interface OnRequestClickListener {
        void onRequestClick(TimeSlot slot, int position);
    }

    public StudentLookupSessionAdapter(List<TimeSlot> list,
                                       OnRequestClickListener listener) {
        this.timeSlotList = list;
        this.requestClickListener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_lookupsession_layout, parent, false); // <-- your XML filename
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = timeSlotList.get(position);

        // Safe Tutor handling
        if (slot.getTutor() != null) {
            String tn = slot.getTutor().getFirstName() + " " + slot.getTutor().getLastName();
            holder.tvTutor.setText(tn);
            holder.tvRating.setText("Rating: " + slot.getTutor().getRating());
        } else {
            holder.tvTutor.setText("Unknown Tutor");
            holder.tvRating.setText("Rating: N/A");
        }

        // Safe Time handling
        ZonedDateTime start = slot.getStartDate();
        ZonedDateTime end   = slot.getEndDate();
        String startStr = (start != null) ? start.toLocalTime().toString() : "N/A";
        String endStr   = (end != null) ? end.toLocalTime().toString() : "N/A";
        holder.tvTime.setText(startStr + " - " + endStr);

        // Button click
        holder.bAccept.setOnClickListener(v ->
                requestClickListener.onRequestClick(slot, position)
        );
    }


    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    // View Holder
    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {

        TextView tvTutor, tvRating, tvTime;
        Button bAccept;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTutor = itemView.findViewById(R.id.tvTutor);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvTime = itemView.findViewById(R.id.tvTime);
            bAccept = itemView.findViewById(R.id.bAccept);
        }
    }

    public void updateData(List<TimeSlot> newList) {
        this.timeSlotList.clear();
        this.timeSlotList.addAll(newList);
        notifyDataSetChanged();
    }

    public void clear() {
        this.timeSlotList.clear();
        notifyDataSetChanged();
    }
}

