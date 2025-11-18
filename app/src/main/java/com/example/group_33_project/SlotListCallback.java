package com.example.group_33_project;

import java.util.List;

public interface SlotListCallback {
    void onSuccess(List<TimeSlot> slots); // so that we can callback a list of slots for UI implementation
    void onFailure(String error);
}
