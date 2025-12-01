package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DeleteAvailabilityAdapter extends RecyclerView.Adapter<DeleteAvailabilityAdapter.TSViewHolder> {

    public interface ActionListener {
        void onDelete(TimeSlot slot);
        void onCancel(TimeSlot slot);
    }

    // IMPORTANT: keep a direct reference to the list provided by the Activity
    private final List<TimeSlot> slots;
    private final ActionListener listener;

    public DeleteAvailabilityAdapter(List<TimeSlot> initial, ActionListener listener) {
        this.slots = (initial != null) ? initial : new ArrayList<>();
        this.listener = listener;
        setHasStableIds(false);
    }

    public void setData(List<TimeSlot> newData) {
        slots.clear();
        if (newData != null) slots.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // This is the *item row* layout (NOT the screen layout)
                .inflate(R.layout.delete_or_cancel_session, parent, false);
        return new TSViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TSViewHolder h, int position) {
        TimeSlot ts = slots.get(position);

        // Format: "MMM dd, yyyy  |  h:mm a - h:mm a"
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");

        String datePart  = (ts.getStartDate() != null) ? dateFmt.format(ts.getStartDate()) : "—";
        String startPart = (ts.getStartDate() != null) ? timeFmt.format(ts.getStartDate()) : "—";
        String endPart   = (ts.getEndDate()   != null) ? timeFmt.format(ts.getEndDate())   : "—";

        String status = ts.getStatus();
        h.tvStatus.setText(status != null ? status : "unknown");
        h.tvTime.setText(datePart + "  |  " + startPart + " - " + endPart);

        // Student name (fallback to "—" instead of literal "null")
        String studentName = "No student";
        if (ts.getStudent() != null) {
            String fn = ts.getStudent().getFirstName();
            String ln = ts.getStudent().getLastName();
            String full = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
            if (!full.isEmpty()) studentName = full;
        }
        h.tvName.setText(studentName);

        h.bDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(ts);
        });

        // if the session is booked, do  not allow tutor to cancel
        if(ts.getStudent()!= null){
            h.bDelete.setVisibility(View.GONE);
        }

        h.bCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(ts);
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public void removeItem(TimeSlot slot) {
        int idx = slots.indexOf(slot);
        if (idx >= 0) {
            slots.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    static class TSViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvTime, tvStatus;
        final Button bDelete, bCancel;

        TSViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvName   = itemView.findViewById(R.id.tvName);
            tvTime   = itemView.findViewById(R.id.tvTime);
            bDelete  = itemView.findViewById(R.id.bDelete);
            bCancel  = itemView.findViewById(R.id.bCancel);
        }
    }
}
