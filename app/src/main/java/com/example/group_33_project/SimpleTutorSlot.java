package com.example.group_33_project;

import java.time.ZonedDateTime;

public class SimpleTutorSlot {
    public ZonedDateTime start;
    public ZonedDateTime end;
    public String status; // "open", "booked", "pending", "empty"
    public boolean isSelected = false;
    public int label;

    public String name = "";

    public boolean clickable; // only true for today/future
    public SimpleTutorSlot (ZonedDateTime start, ZonedDateTime end, String status, int num) {
        this.start = start;
        this.end = end;
        this.status = status;
        this.label = num;
    }

    public boolean isPast() {
        return end.isBefore(ZonedDateTime.now());
    }

    public void setClickable(boolean c){ clickable = c; }
}
