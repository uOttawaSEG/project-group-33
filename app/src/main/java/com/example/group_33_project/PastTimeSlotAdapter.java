package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PastTimeSlotAdapter extends RecyclerView.Adapter<PastTimeSlotAdapter.PastVH> {

    private final List<TimeSlot> items = new ArrayList<>();
    private final Tutor tutorForHeader; // optional – show tutor name on each row

    public PastTimeSlotAdapter(Tutor tutorForHeader) {
        this.tutorForHeader = tutorForHeader;
    }

    public void setData(List<TimeSlot> newItems) {
        items.clear();
        if (newItems != null)
            items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PastVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // CHANGE THIS IF YOUR ROW LAYOUT NAME IS DIFFERENT
                .inflate(R.layout.past_session, parent, false);
        return new PastVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PastVH h, int position) {
        TimeSlot slot = items.get(position);

        // Tutor name
        String tutorName = (tutorForHeader != null)
                ? tutorForHeader.getFirstName() + " " + tutorForHeader.getLastName()
                : (slot.getTutor() != null
                ? slot.getTutor().getFirstName() + " " + slot.getTutor().getLastName()
                : "Unknown Tutor");
        h.tvTutorName.setText(tutorName);

        // Format date & time
        ZonedDateTime start = slot.getStartDate();
        ZonedDateTime end = slot.getEndDate();

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");

        h.tvDate.setText(start != null ? "Date: " + dateFmt.format(start) : "Date: —");
        h.tvStart.setText(start != null ? "Start: " + timeFmt.format(start) : "Start: —");
        h.tvEnd.setText(end != null ? "End: " + timeFmt.format(end) : "End: —");

        // Student email or null
        Student s = slot.getStudent();
        h.tvStudent.setText("Student: " + (s != null && s.getEmail() != null ? s.getEmail() : "null"));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PastVH extends RecyclerView.ViewHolder {

        final TextView tvTutorName, tvDate, tvStart, tvEnd, tvStudent;

        PastVH(@NonNull View itemView) {
            super(itemView);
            tvTutorName = itemView.findViewById(R.id.tvTutorName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStart = itemView.findViewById(R.id.tvStart);
            tvEnd = itemView.findViewById(R.id.tvEnd);
            tvStudent = itemView.findViewById(R.id.tvStudent);
        }
    }
}
