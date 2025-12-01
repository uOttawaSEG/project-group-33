package com.example.group_33_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentPastSessionAdapter extends RecyclerView.Adapter<StudentPastSessionAdapter.PastSessionViewHolder> {

    public interface OnRateClickListener {
        void onRate(TimeSlot slot, int rating, int position);
    }

    private List<TimeSlot> sessions;
    private OnRateClickListener listener;

    public StudentPastSessionAdapter(List<TimeSlot> sessions, OnRateClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PastSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_pastsession_layout, parent, false); // change if needed
        return new PastSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastSessionViewHolder holder, int position) {
        TimeSlot slot = sessions.get(position);

        String n = slot.getTutor().getFirstName() + " " + slot.getTutor().getLastName();
        holder.tvTutor.setText(n);
        holder.tvTime.setText(formatTime(slot.getStartDate(), slot.getEndDate()));
        Context ctx = holder.itemView.getContext();

        if (slot.isRated()) {
            String r = "Rated";
            holder.tvRating.setText(r); // assuming getRating() exists
            holder.rateButton.setVisibility(View.GONE);
            holder.rateInput.setVisibility(View.GONE);
        } else {
            holder.tvRating.setText("Not Rated");

            // Show input + button
            holder.rateButton.setVisibility(View.VISIBLE);
            holder.rateInput.setVisibility(View.VISIBLE);

            holder.rateButton.setOnClickListener(v -> {
                String input = holder.rateInput.getText().toString().trim();

                // Validate input is integer 1–5
                int rating;
                try {
                    rating = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    Toast.makeText(ctx, "Rating must be an integer", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rating < 1 || rating > 5) {
                    Toast.makeText(ctx, "Rating must be in range from 1 to 5", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Trigger callback
                listener.onRate(slot, rating, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    static class PastSessionViewHolder extends RecyclerView.ViewHolder {

        TextView tvTutor, tvCourse, tvTime, tvRating;
        EditText rateInput;
        Button rateButton;

        public PastSessionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTutor = itemView.findViewById(R.id.tvTutor);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRating = itemView.findViewById(R.id.tvRating);
            rateInput = itemView.findViewById(R.id.rateTutor);
            rateButton = itemView.findViewById(R.id.bRATE);
        }
    }

    private String formatTime(ZonedDateTime start, ZonedDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        return start.format(formatter) + " - " + end.format(formatter);
    }
}

