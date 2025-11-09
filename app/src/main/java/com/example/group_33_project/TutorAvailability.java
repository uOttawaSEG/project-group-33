package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TutorAvailability extends AppCompatActivity {

    private RecyclerView rvSlots;
    private TextView tvWeekLabel;
    private ZonedDateTime currentWeekStart;
    private SlotCreateTutorAdapter adapter;

    private Button btnNextWeek, btnPrevWeek, btnCreateAvailability, btnBack;

    private final List<SimpleTutorSlot> allSlots = new ArrayList<>();
    private List<SimpleTutorSlot> selectedSlots = new ArrayList<>();

    private Tutor currentTutor;
    private TutorHandling tutorH;

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen10_availabilitytut);

        rvSlots = findViewById(R.id.rvSlots);
        tvWeekLabel = findViewById(R.id.tvWeekLabel);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnCreateAvailability = findViewById(R.id.btnCreateAvailability);
        btnCreateAvailability.setVisibility(View.GONE);
        btnBack = findViewById(R.id.screen10_back);

        tutorH = new TutorHandling();
        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor");

        if (currentTutor == null) {
            Toast.makeText(this, "Error: Tutor not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set current week start to Sunday
        currentWeekStart = ZonedDateTime.now()
                .with(DayOfWeek.SUNDAY)
                .truncatedTo(ChronoUnit.DAYS);

        setupWeekNavigation();
        setupAdapter();
        generateSlotsForWeek(currentWeekStart);

        btnCreateAvailability.setOnClickListener(v -> handleCreateAvailability());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorAvailability.this, TutorIn.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
            finish();
        });
    }

    // ------------------------------------------------------------------------
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

    private void updateWeekLabel() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        ZonedDateTime weekEnd = currentWeekStart.plusDays(6);
        tvWeekLabel.setText(fmt.format(currentWeekStart) + " - " + fmt.format(weekEnd));
    }

    // ------------------------------------------------------------------------
    private void generateSlotsForWeek(ZonedDateTime weekStart) {
        allSlots.clear();
        count = 1;

        // 9:00 → 21:00, 30-min slots, vertical (time-first)
        for (int hour = 9; hour < 21; hour++) {
            for (int min = 0; min < 60; min += 30) {
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                    ZonedDateTime slotStart = weekStart.plusDays(dayOffset)
                            .withHour(hour)
                            .withMinute(min)
                            .truncatedTo(ChronoUnit.MINUTES);
                    ZonedDateTime slotEnd = slotStart.plusMinutes(30);
                    allSlots.add(new SimpleTutorSlot(slotStart, slotEnd, "empty", count++));
                }
            }
        }

        updateWeekLabel();

        ZonedDateTime weekEnd = weekStart.plusDays(6);

        // Load open and booked slots
        tutorH.getAllSlotsByStatus("open", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allTimeSlots) {
                updateSlotsWithTimeSlots(allTimeSlots, weekStart, weekEnd);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed to load open slots: " + error);
            }
        });

        tutorH.getAllSlotsByStatus("booked", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allTimeSlots) {
                updateSlotsWithTimeSlots(allTimeSlots, weekStart, weekEnd);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed to load booked slots: " + error);
            }
        });
    }

    private void updateSlotsWithTimeSlots(List<TimeSlot> allTimeSlots, ZonedDateTime weekStart, ZonedDateTime weekEnd) {
        int sessionCounter = 1; // Start numbering sessions from 1

        for (TimeSlot ts : allTimeSlots) {
            if (ts.getTutor() == null || ts.getTutor().getEmail() == null) continue;
            if (!ts.getTutor().getEmail().equals(currentTutor.getEmail())) continue;

            ZonedDateTime tStart = ts.getStartDate().truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime tEnd = ts.getEndDate().truncatedTo(ChronoUnit.MINUTES);

            if (tEnd.isBefore(weekStart) || tStart.isAfter(weekEnd)) continue;

            // Mark all overlapping SimpleTutorSlots with the SAME session number
            for (SimpleTutorSlot s : allSlots) {
                if (s.start.isBefore(tEnd) && s.end.isAfter(tStart)) {
                    s.status = ts.getStatus();
                    s.name = "session " + sessionCounter; // same number for all slots in this session
                }
            }

            sessionCounter++; // Increment only per tutor session, not per slot
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }


    // ------------------------------------------------------------------------
    private void setupAdapter() {
        adapter = new SlotCreateTutorAdapter(allSlots, selected -> {
            selectedSlots = new ArrayList<>(selected);
            btnCreateAvailability.setVisibility(selectedSlots.isEmpty() ? View.GONE : View.VISIBLE);
        });

        // 7 columns = 7 days, vertical scroll through time
        rvSlots.setLayoutManager(new GridLayoutManager(this, 7, RecyclerView.VERTICAL, false));
        rvSlots.setAdapter(adapter);
    }

    // ------------------------------------------------------------------------
    private void handleCreateAvailability() {
        if (selectedSlots.isEmpty()) return;

        List<SimpleTutorSlot> slotsToCreate = new ArrayList<>(selectedSlots);
        selectedSlots.clear();
        btnCreateAvailability.setVisibility(View.GONE);

        // Filter already taken slots
        slotsToCreate = slotsToCreate.stream()
                .filter(s -> "empty".equals(s.status))
                .collect(Collectors.toList());

        if (slotsToCreate.isEmpty()) return;

        // Group by day using HashMap
        Map<DayOfWeek, List<SimpleTutorSlot>> slotsByDay = new HashMap<>();
        for (SimpleTutorSlot s : slotsToCreate) {
            slotsByDay.computeIfAbsent(s.start.getDayOfWeek(), k -> new ArrayList<>()).add(s);
        }

        // Manual day order
        DayOfWeek[] daysOrder = {DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY};

        List<ZonedDateTime[]> sessionsToCreate = new ArrayList<>();

        for (DayOfWeek day : daysOrder) {
            List<SimpleTutorSlot> daySlots = slotsByDay.get(day);
            if (daySlots == null) continue;

            daySlots.sort(Comparator.comparing(s -> s.start));

            ZonedDateTime rangeStart = daySlots.get(0).start;
            ZonedDateTime prevEnd = daySlots.get(0).end;

            for (int i = 1; i < daySlots.size(); i++) {
                SimpleTutorSlot current = daySlots.get(i);
                if (!current.start.equals(prevEnd)) {
                    sessionsToCreate.add(new ZonedDateTime[]{rangeStart, prevEnd});
                    rangeStart = current.start;
                }
                prevEnd = current.end;
            }
            sessionsToCreate.add(new ZonedDateTime[]{rangeStart, prevEnd});
        }

        createAllAvailabilitySequential(sessionsToCreate);
    }

    private void createAllAvailabilitySequential(List<ZonedDateTime[]> sessions) {
        if (sessions.isEmpty()) {
            runOnUiThread(() -> generateSlotsForWeek(currentWeekStart));
            return;
        }

        ZonedDateTime[] session = sessions.remove(0);
        tutorH.createNewAvailability(currentTutor, session[0], session[1], false, new TutorCallback() {
            @Override
            public void onSuccess(String msg) {
                updateLocalSlots(session[0], session[1]);
                createAllAvailabilitySequential(sessions);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed: " + error);
                createAllAvailabilitySequential(sessions);
            }
        });
    }

    private void updateLocalSlots(ZonedDateTime start, ZonedDateTime end) {
        for (SimpleTutorSlot s : allSlots) {
            if ("empty".equals(s.status) && !s.start.isBefore(start) && !s.end.isAfter(end)) {
                s.status = "open";
            }
        }
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }
}

