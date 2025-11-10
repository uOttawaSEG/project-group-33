package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class deleteAvailabilityAdapterS extends RecyclerView.Adapter<deleteAvailabilityAdapterS.TSViewHolder> {

    private OnAccountActionListener listener;
    private List<TimeSlot> slots;

    // Interface for callback actions (delete, cancel, etc.)
    public interface OnAccountActionListener {
        void onDelete(TimeSlot slot);
        void onCancel(TimeSlot slot);
    }

    // Constructor
    public deleteAvailabilityAdapterS(List<TimeSlot> slots, OnAccountActionListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delete_or_cancel_session, parent, false);
        return new TSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TSViewHolder holder, int position) {
        TimeSlot ts = slots.get(position);

        String time = "";

        time = ts.startDate.format(DateTimeFormatter.ofPattern("MMMM - dd - hh:mm a - yyyy")) + " " +  ts.endDate.format(DateTimeFormatter.ofPattern("MMMM - dd - hh:mm a - yyyy"));

        // Fill the data from the TimeSlot object
        holder.tvName.setText("");

        if (ts.getStudent() != null){
            holder.tvName.setText(ts.getStudent().getFirstName() + " " + ts.getStudent().getLastName());
        }

        holder.tvTime.setText(time);
        holder.tvStatus.setText(ts.getStatus());

        // Set up click listeners
        holder.btDelete.setOnClickListener(v -> {
            listener.onDelete(ts);
            //Toast.makeText(v.getContext(), "Deleted " + ts.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.btCancel.setOnClickListener(v -> {
            listener.onCancel(ts);
            String tim = ts.startDate.format(DateTimeFormatter.ofPattern("MMMM - dd - hh:mm a - yyyy")) + " " +  ts.endDate.format(DateTimeFormatter.ofPattern("MMMM - dd - hh:mm a - yyyy"));
            Toast.makeText(v.getContext(), "Cancelled " + tim, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    // ViewHolder class
    public static class TSViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvStatus;
        Button btDelete, btCancel;

        public TSViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btDelete = itemView.findViewById(R.id.bDelete);
            btCancel = itemView.findViewById(R.id.bCancel);
        }
    }
}
