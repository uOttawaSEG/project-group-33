package com.example.group_33_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Simple RecyclerView adapter for past TimeSlot items.
 * No DiffUtil; uses notifyDataSetChanged().
 * Binds tutor name (or student name if you prefer) and a formatted time range.
 */
public class PastTimeSlotAdapter extends RecyclerView.Adapter<PastTimeSlotAdapter.TimeSlotViewHolder> {

    private final List<TimeSlot> slots;

    public PastTimeSlotAdapter(List<TimeSlot> slots) {
        this.slots = slots;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.past_session, parent, false);
        return new TimeSlotViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlot slot = slots.get(position);

        // --- NAME BIND ---
        // Prefer tutor name; fall back to student name or email as available
        String nameText = "Unknown";
        if (slot.getTutor() != null) {
            Tutor t = slot.getTutor();
            if (t.getFirstName() != null || t.getLastName() != null) {
                nameText = (nullToEmpty(t.getFirstName()) + " " + nullToEmpty(t.getLastName())).trim();
            } else if (t.getEmail() != null) {
                nameText = t.getEmail();
            }
        } else if (slot.getStudent() != null) {
            Student s = slot.getStudent();
            if (s.getFirstName() != null || s.getLastName() != null) {
                nameText = (nullToEmpty(s.getFirstName()) + " " + nullToEmpty(s.getLastName())).trim();
            } else if (s.getEmail() != null) {
                nameText = s.getEmail();
            }
        }
        holder.tvName.setText(nameText.isEmpty() ? "Unknown" : nameText);

        // --- TIME BIND ---
        // Replace-this-section meaning (your earlier question): this is exactly where
        // you compute a readable time string from the slot and put it into tvTime.
        holder.tvTime.setText(buildTimeText(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;
        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    /**
     * Attempts several common shapes of a TimeSlot model to build a display string.
     * Adjust to your actual model (getStartMillis/getEndMillis, getStartTime/getEndTime, etc.).
     */
    private String buildTimeText(TimeSlot slot) {
        // 1) If your model already has a pretty string:
        try {
            // e.g., slot.getDisplayTime() or slot.getTimeRangeString()
            java.lang.reflect.Method m = slot.getClass().getMethod("getDisplayTime");
            Object val = m.invoke(slot);
            if (val instanceof String && !((String) val).isEmpty()) return (String) val;
        } catch (Exception ignored) {}

        try {
            java.lang.reflect.Method m = slot.getClass().getMethod("getTimeRangeString");
            Object val = m.invoke(slot);
            if (val instanceof String && !((String) val).isEmpty()) return (String) val;
        } catch (Exception ignored) {}

        // 2) If you have millis fields:
        try {
            long start = (long) slot.getClass().getMethod("getStartMillis").invoke(slot);
            long end   = (long) slot.getClass().getMethod("getEndMillis").invoke(slot);
            return formatRangeMillis(start, end);
        } catch (Exception ignored) {}

        // 3) If you have java.util.Date fields:
        try {
            Object startDate = slot.getClass().getMethod("getStartTime").invoke(slot);
            Object endDate   = slot.getClass().getMethod("getEndTime").invoke(slot);
            if (startDate instanceof java.util.Date && endDate instanceof java.util.Date) {
                return formatRangeMillis(((java.util.Date) startDate).getTime(),
                        ((java.util.Date) endDate).getTime());
            }
        } catch (Exception ignored) {}

        // 4) Fallback
        return slot.toString();
    }

    private String formatRangeMillis(long startMs, long endMs) {
        DateTimeFormatter dayFmt  = DateTimeFormatter.ofPattern("EEE, MMM d yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");
        var start = Instant.ofEpochMilli(startMs).atZone(ZoneId.systemDefault());
        var end   = Instant.ofEpochMilli(endMs).atZone(ZoneId.systemDefault());
        String dayPart  = dayFmt.format(start);
        String timePart = timeFmt.format(start) + " – " + timeFmt.format(end);
        return dayPart + " • " + timePart;
    }

    // Optional helpers if you want equals()/getId() style operations without DiffUtil:

    /** Removes a slot by ID (if your TimeSlot has getId()) and returns true if removed. */
    public boolean removeById(String id) {
        if (id == null) return false;
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot s = slots.get(i);
            String sid = tryGetId(s);
            if (id.equals(sid)) {
                slots.remove(i);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    /** Checks if a slot already exists by ID. */
    public boolean containsById(String id) {
        if (id == null) return false;
        for (TimeSlot s : slots) {
            if (id.equals(tryGetId(s))) return true;
        }
        return false;
    }

    private String tryGetId(TimeSlot s) {
        try {
            Object id = s.getClass().getMethod("getId").invoke(s);
            return id == null ? null : String.valueOf(id);
        } catch (Exception e) {
            return null;
        }
    }
}
