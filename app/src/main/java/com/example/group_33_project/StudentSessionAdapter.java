package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class StudentSessionAdapter extends RecyclerView.Adapter<StudentSessionAdapter.ViewHolder> {

    private final List<TimeSlot> sessionList;
    private final OnCancelListener cancelListener;

    public interface OnCancelListener {
        void onCancelSession(TimeSlot slot);
    }

    public StudentSessionAdapter(List<TimeSlot> sessionList, OnCancelListener cancelListener) {
        this.sessionList = sessionList;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_delete_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = sessionList.get(position);

        // CHECKS THE STATUS OF THE SESSION: booked or pending
        holder.tvStatus.setText(String.format("Status: %s", capitalize(slot.getStatus())));

        //DISPLAYS THE TUTOR NAME AND THE COURSE
        String tutorInfo = String.format("Tutor: %s", slot.getTutor().getFirstName() + " " + slot.getTutor().getLastName());
        holder.tvName.setText(tutorInfo);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE, MMM dd | hh:mm a");
        String timeInfo = String.format(
                "%s - %s",
                slot.getStartDate().format(dateFormat),
                slot.getEndDate().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
        holder.tvTime.setText(timeInfo);

        // STUDENT CAN CANCEL IF THE SESSION IS MORE THAN 24 HOURS AWAY FROM NOW
        // STUDENT CAN CANCEL IF THE SESSION IS NOT "cancelled" or "rejected" BY THE TUTOR
        ZonedDateTime now = ZonedDateTime.now(slot.getStartDate().getZone());
        Duration durationUntilStart = Duration.between(now, slot.getStartDate());
        boolean isMoreThan24HoursAway = durationUntilStart.toHours() > 24;

        if (isMoreThan24HoursAway && !"cancelled".equals(slot.getStatus()) && !"rejected".equals(slot.getStatus())) {
            holder.bCancel.setVisibility(View.VISIBLE);
            holder.bCancel.setEnabled(true);

            holder.bCancel.setOnClickListener(v -> cancelListener.onCancelSession(slot));
        } else {
            // HIDES THE CANCEL BUTTON IF THE STUDENT CANNOT CANCEL
            holder.bCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    // HELPER METHOD TO CAPITALIZE THE STATUS ("booked" -> "Booked")
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvStatus;
        public final TextView tvName;
        public final TextView tvTime;
        public final Button bCancel;

        public ViewHolder(View view) {
            super(view);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvName = view.findViewById(R.id.tvName);
            tvTime = view.findViewById(R.id.tvTime);
            bCancel = view.findViewById(R.id.bCancel);
        }
    }

    // Method to update the list
    public void setSessions(List<TimeSlot> newSessions) {
        sessionList.clear();
        if (newSessions != null) {
            sessionList.addAll(newSessions);
        }
        notifyDataSetChanged();
    }
}
