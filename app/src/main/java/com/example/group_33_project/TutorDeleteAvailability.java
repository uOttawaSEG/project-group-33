package com.example.group_33_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TutorDeleteAvailability extends AppCompatActivity {


    private RecyclerView recyclerView;
    private deleteAvailabilityAdapterS adapter;
    private List<TimeSlot> slotList;

    private TutorHandling tutorH;

    Button btnBack;

    private Tutor currentTutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen14_deletesessions);

        recyclerView = findViewById(R.id.RecyclerView14);

        slotList = new ArrayList<>();

        tutorH = new TutorHandling();

        btnBack = findViewById(R.id.screen14_back);

        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor");

        if (currentTutor == null) {
            Toast.makeText(this, "Error: Tutor not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tutorH.getAllSlotsByStatus("open", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allTimeSlots) {
                updateSlotsWithTimeSlots(allTimeSlots);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed to load open slots: " + error);
            }
        });

        tutorH.getAllSlotsByStatus("booked", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allTimeSlots) {
                updateSlotsWithTimeSlots(allTimeSlots);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed to load booked slots: " + error);
            }
        });

        adapter = new deleteAvailabilityAdapterS(slotList, new deleteAvailabilityAdapterS.OnAccountActionListener() {
            @Override
            public void onDelete(TimeSlot slot) {
                tutorH.deleteAvailability(slot, new TutorCallback() {
                    @Override
                    public void onSuccess(String msg) {

                    }

                    @Override
                    public void onFailure(String msg) {
                        Toast.makeText(TutorDeleteAvailability.this, "Unsuccesful deletion" , Toast.LENGTH_SHORT).show();
                    }
                });
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

            @Override
            public void onCancel(TimeSlot slot) {
                slot.setStatus("cancelled");
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                //Toast.makeText(TutorDeleteAvailability.this, "Cancelling " + slot.getName(), Toast.LENGTH_SHORT).show();
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        //BUTTON TO GO BACK TO THE LOGGED IN SCREEN
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDeleteAvailability.this, TutorIn.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
            finish();
        });}
    private void updateSlotsWithTimeSlots(List<TimeSlot> allTimeSlots) {
        int sessionCounter = 1; // Start numbering sessions from 1

        for (TimeSlot ts : allTimeSlots) {
            if (ts.getTutor() == null || ts.getTutor().getEmail() == null) continue;
            if (!ts.getTutor().getEmail().equals(currentTutor.getEmail())) continue;

            ZonedDateTime tStart = ts.getStartDate().truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime tEnd = ts.getEndDate().truncatedTo(ChronoUnit.MINUTES);

            slotList.add(ts);
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }


    //CODE FOR THE DELETE FUNCTION THAT LOOKS LIKE A CALENDAR VIEW
    //A LOT HARDER TO FIGURE OUT

    //THIS VERSION CRASHES
    //IF WE WANT THIS TO BE CALENDER-LIKE AS WELL FIX THIS STARTED CODE
    //INITIALLY MADE BY IVANNA

    /*private RecyclerView rvSlots;
    private TextView tvWeekLabel;
    private ZonedDateTime currentWeekStart;
    private SlotDeleteTutorAdapter adapter;

    private Button btnNextWeek, btnPrevWeek, btnDelete, btnCancel, btnBack;

    private List<SimpleTutorSlot> allSlots = new ArrayList<>();
    private List<SimpleTutorSlot> selectedSlots = new ArrayList<>();

    private Tutor currentTutor;
    private TutorHandling tutorH;

    private boolean needsApproval;

    private Map<SimpleTutorSlot, TimeSlot> simpleTime = new HashMap<>();
    private Map<SimpleTutorSlot, List<SimpleTutorSlot>> simpleG = new HashMap<>();

    private final Object slotLock = new Object();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen14_deletesessions);

        rvSlots = findViewById(R.id.rvSlots14);
        tvWeekLabel = findViewById(R.id.tvWeekLabel14);
        btnNextWeek = findViewById(R.id.btnNextWeek14);
        btnPrevWeek = findViewById(R.id.btnPrevWeek14);
        btnDelete = findViewById(R.id.btnDelete14);
        btnCancel = findViewById(R.id.btnCancel14);
        btnBack = findViewById(R.id.screen14_back);
        tutorH = new TutorHandling();

        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor");
        if (currentTutor == null) {
            Toast.makeText(this, "Error: Tutor not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("SCREEN_SETTINGS", Context.MODE_PRIVATE);
        needsApproval = prefs.getBoolean("BUTTON_VISIBILITY", false);

        currentWeekStart = ZonedDateTime.now()
                .with(DayOfWeek.SUNDAY)
                .truncatedTo(ChronoUnit.DAYS);

        setupWeekNavigation();
        setupAdapter();
        generateSlotsForWeek(currentWeekStart);

        btnDelete.setOnClickListener(v -> handleDelete());

        btnCancel.setOnClickListener(v -> cancel());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDeleteAvailability.this, TutorIn.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
            finish();
        });
    }

    private void setupWeekNavigation() {
        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            generateSlotsForWeek(currentWeekStart);
        });

        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            generateSlotsForWeek(currentWeekStart);
        });

        updateWeekLabel();
    }

    //UPDATE THE WEEK EVERY TIME THE BUTTONS TO GO TO/FROM DIFFERENT WEEKS ARE CLICKED
    private void updateWeekLabel() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        ZonedDateTime weekEnd = currentWeekStart.plusDays(6);
        tvWeekLabel.setText(fmt.format(currentWeekStart) + " - " + fmt.format(weekEnd));
    }

    //GENEREATE AND THEN PERSONALIZE SLOTS FOR THIS WEEK
    private final AtomicInteger pendingFetches = new AtomicInteger(0);

    private void generateSlotsForWeek(ZonedDateTime weekStart) {
        allSlots.clear();
        simpleTime.clear();
        simpleG.clear();

        // build dummy slots
        for (int hour = 9; hour < 21; hour++) {
            for (int min = 0; min < 60; min += 30) {
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                    ZonedDateTime slotStart = weekStart.plusDays(dayOffset)
                            .withHour(hour).withMinute(min)
                            .truncatedTo(ChronoUnit.MINUTES);
                    allSlots.add(new SimpleTutorSlot(slotStart, slotStart.plusMinutes(30), "empty"));
                }
            }
        }

        updateWeekLabel();

        // count how many async calls we expect
        pendingFetches.set(4);

        String[] statuses = {"open", "booked", "pending", "cancelled"};
        ZonedDateTime weekEnd = weekStart.plusDays(6);

        for (String status : statuses) {
            tutorH.getAllSlotsByStatus(status, new SlotListCallback() {
                @Override
                public void onSuccess(List<TimeSlot> allTimeSlots) {
                    applyTimeSlotsThreadSafe(allTimeSlots, weekStart, weekEnd);
                }

                @Override
                public void onFailure(String error) {
                    Log.e("TutorDelete", "Failed to load " + status + ": " + error);
                    checkFinishBatch();
                }
            });
        }
    }

    private void applyTimeSlotsThreadSafe(List<TimeSlot> allTimeSlots, ZonedDateTime weekStart, ZonedDateTime weekEnd) {
        synchronized (slotLock) {
            int sessionCounter = 1;
            for (TimeSlot ts : allTimeSlots) {
                if (ts == null || ts.getTutor() == null) continue;
                if (!ts.getTutor().getEmail().equals(currentTutor.getEmail())) continue;

                ZonedDateTime tStart = ts.getStartDate().truncatedTo(ChronoUnit.MINUTES);
                ZonedDateTime tEnd = ts.getEndDate().truncatedTo(ChronoUnit.MINUTES);
                if (tEnd.isBefore(weekStart) || tStart.isAfter(weekEnd)) continue;

                ArrayList<SimpleTutorSlot> group = new ArrayList<>();
                for (SimpleTutorSlot s : allSlots) {
                    if (s.start.isBefore(tEnd) && s.end.isAfter(tStart)) {
                        s.status = ts.getStatus();
                        s.name = "session " + sessionCounter;
                        group.add(s);
                        simpleTime.put(s, ts);
                    }
                }

                for (SimpleTutorSlot s : group) {
                    simpleG.put(s, group);
                }
                sessionCounter++;
            }
        }
        checkFinishBatch();
    }

    private void checkFinishBatch() {
        if (pendingFetches.decrementAndGet() == 0) {
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }


    //INITIALIZE ADANTER
    private void setupAdapter() {
        adapter = new SlotDeleteTutorAdapter(allSlots, selected -> {
            selectedSlots = new ArrayList<>(selected);
            btnDelete.setVisibility(selectedSlots.isEmpty() ? View.GONE : View.VISIBLE);
            btnCancel.setVisibility(selectedSlots.isEmpty() ? View.GONE : View.VISIBLE);
        });

        //VERTICAL LAYOUT LIKE CALENDAR
        rvSlots.setLayoutManager(new GridLayoutManager(this, 7, RecyclerView.VERTICAL, false));
        rvSlots.setAdapter(adapter);
    }

    //METHOD TO DELETE
    private void handleDelete() {
        if (selectedSlots.isEmpty()) return;

        List<SimpleTutorSlot> slotsToDelete = new ArrayList<>(selectedSlots);

        selectedSlots.clear();
        btnDelete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        for(SimpleTutorSlot s: slotsToDelete){
            List<SimpleTutorSlot> oneSession = simpleG.get(s);
            TimeSlot toRemove = simpleTime.get(s);

            if(oneSession != null && toRemove != null) {
                tutorH.deleteAvailability(toRemove, new TutorCallback() {
                    @Override
                    public void onSuccess(String msg) {

                    }

                    @Override
                    public void onFailure(String msg) {

                    }
                }); /*{
                    @Override
                    public void onSuccess(List<TimeSlot> allTimeSlots) {
                        Toast.makeText(getApplicationContext(), "deleted succesfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("TutorAvailability", "Failed to load booked slots: " + error);
                    }
                });

                for (SimpleTutorSlot sl : oneSession) {
                    simpleG.remove(s);
                    simpleTime.remove(s);
                }
            }
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());

    }

    //CANCEL METHOD
    private void cancel() {
        if (selectedSlots.isEmpty()) return;

        List<SimpleTutorSlot> slotsToDelete = new ArrayList<>(selectedSlots);

        selectedSlots.clear();
        btnDelete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        for(SimpleTutorSlot s: slotsToDelete){
            List<SimpleTutorSlot> oneSession = simpleG.get(s);
            TimeSlot toCancel = simpleTime.get(s);

            if(oneSession != null && toCancel != null) {

                toCancel.setStatus("cancelled");

                for (SimpleTutorSlot sl : oneSession) {
                    simpleG.remove(s);
                    simpleTime.remove(s);
                }
            }
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());

    }*/
}

