package com.example.group_33_project;

import android.graphics.Color;
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

public class SlotCreateTutorAdapter extends RecyclerView.Adapter<SlotCreateTutorAdapter.SlotViewHolder> {
    private List<SimpleTutorSlot> slots;               // All slots in the grid
    private List<SimpleTutorSlot> selectedSlots = new ArrayList<>(); // Multi-selection
    private OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotsSelected(List<SimpleTutorSlot> selectedSlots);
    }

    public SlotCreateTutorAdapter(List<SimpleTutorSlot> slots, OnSlotClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simpleslot_tutorslot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        SimpleTutorSlot slot = slots.get(position);

// Determine if past
        ZonedDateTime now = ZonedDateTime.now();
        boolean isPast = slot.start.isBefore(now);

// Label: only booked/open slots show "Slot X", empty/past slots show nothing
        if ("booked".equals(slot.status)||"open".equals(slot.status)) {
            holder.tvSlot.setText(slot.name);
            //holder.tvSlot.setText("Slot " + slot.daySlotNumber); // daySlotNumber = 1,2,3 per day
        } else {
            holder.tvSlot.setText("");
        }

// Background color
        if (isPast) {
            holder.tvSlot.setBackgroundColor(Color.GRAY); // past slots
        } else if (selectedSlots.contains(slot)) {
            holder.tvSlot.setBackgroundColor(Color.GREEN); // selected
        } else {
            switch (slot.status) {
                case "booked":
                    holder.tvSlot.setBackgroundColor(Color.RED);
                    break;
                case "pending":
                    holder.tvSlot.setBackgroundColor(Color.YELLOW);
                    break;
                case "open":
                    holder.tvSlot.setBackgroundColor(Color.BLUE);
                    break;
                default:
                    holder.tvSlot.setBackgroundColor(Color.LTGRAY); // unselected future slot
                    break;
            }
        }

// Enable only clickable future open slots
        holder.tvSlot.setEnabled(!isPast && "empty".equals(slot.status));

        holder.tvSlot.setOnClickListener(v -> {
            if (!holder.tvSlot.isEnabled()) return;

            if (selectedSlots.contains(slot)) {
                selectedSlots.remove(slot);
            } else {
                selectedSlots.add(slot);
            }

            notifyItemChanged(position);
            if (listener != null) listener.onSlotsSelected(selectedSlots);
        });

    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public void clearSelection() {
        selectedSlots.clear();
        notifyDataSetChanged();
    }

    public List<SimpleTutorSlot> getSelectedSlots() {
        return selectedSlots;
    }

    public static class SlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlot;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlot = itemView.findViewById(R.id.tvSlot);
        }
    }
}