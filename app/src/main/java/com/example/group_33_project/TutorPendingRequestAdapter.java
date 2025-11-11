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

public class TutorPendingRequestAdapter extends RecyclerView.Adapter<TutorPendingRequestAdapter.PendingViewHolder> {

    public interface ActionListener {
        void onApprove(TimeSlot slot);
        void onDeny(TimeSlot slot);
    }

    // IMPORTANT: keep a direct reference to the list provided by the Activity
    private final List<TimeSlot> slots;
    private final ActionListener listener;

    public TutorPendingRequestAdapter(List<TimeSlot> initial, ActionListener listener) {
        this.slots = (initial != null) ? initial : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // This is the *item row* layout (NOT the screen layout)
                .inflate(R.layout.pending_session, parent, false);
        return new PendingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull  PendingViewHolder h, int position) {
        TimeSlot ts = slots.get(position);

        // Format: "MMM dd, yyyy  |  h:mm a - h:mm a"
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");

        String datePart  = (ts.getStartDate() != null) ? dateFmt.format(ts.getStartDate()) : "—";
        String startPart = (ts.getStartDate() != null) ? timeFmt.format(ts.getStartDate()) : "—";
        String endPart   = (ts.getEndDate()   != null) ? timeFmt.format(ts.getEndDate())   : "—";

        String name = ts.getStudent().getFirstName() + " " + ts.getStudent().getLastName();
        h.tvTime.setText(datePart + "  |  " + startPart + " - " + endPart);
        h.tvName.setText(name);

        h.bApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(ts);
        });

        h.bDeny.setOnClickListener(v -> {
            if (listener != null) listener.onDeny(ts);
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class PendingViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvTime;
        public Button bApprove, bDeny;

        PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime  = itemView.findViewById(R.id.tvTime);
            bApprove  = itemView.findViewById(R.id.bAccept);
            bDeny  = itemView.findViewById(R.id.bDeny);
        }
    }
}
