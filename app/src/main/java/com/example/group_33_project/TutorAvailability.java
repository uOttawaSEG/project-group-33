package com.example.group_33_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.time.temporal.TemporalAdjusters;
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
    private boolean needsApproval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen10_availabilitytut);

        //FIND BUTTONS AND SET INSTANCE VARIABLES
        rvSlots = findViewById(R.id.rvSlots);
        tvWeekLabel = findViewById(R.id.tvWeekLabel);
        btnNextWeek = findViewById(R.id.btnNextWeek);
        btnPrevWeek = findViewById(R.id.btnPrevWeek);
        btnCreateAvailability = findViewById(R.id.btnCreateAvailability);
        btnCreateAvailability.setVisibility(View.GONE);
        btnBack = findViewById(R.id.screen10_back);
        tutorH = new TutorHandling();
        //FIND THIS SPECIFIC TUTOR FOR FUTURE FILTERING
        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor");


        if (currentTutor == null) {
            Toast.makeText(this, "Error: Tutor not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("ScreenSettings", Context.MODE_PRIVATE);
        needsApproval = !prefs.getBoolean("ButtonVisibility", false); // logic was mistakenly created backwards, so just take !

        //SETUP CURRENT WEEKS SLOTS
        currentWeekStart = ZonedDateTime.now(ZoneId.of("America/Toronto"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)) // fix to ensure we start on the correct week
                .truncatedTo(ChronoUnit.DAYS);

        setupWeekNavigation();
        setupAdapter();
        generateSlotsForWeek(currentWeekStart);

        //BUTTON TO CREATE NEW CLASSES
        btnCreateAvailability.setOnClickListener(v -> handleCreateAvailability());

        //BUTTON TO GO BACK TO THE LOGGED IN SCREEN
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorAvailability.this, TutorIn.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
            finish();
        });
    }

    //BUTTONS TO GO BACK/FORWARD IN WEEKS
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
    private void generateSlotsForWeek(ZonedDateTime weekStart) {
        allSlots.clear();
        count = 1;
        ZonedDateTime weekEndExclusive = weekStart.plusDays(7); // next Sunday 00:00

        //dummy slots
        for (int hour = 9; hour < 21; hour++) {
            for (int min = 0; min < 60; min += 30) {
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                    ZonedDateTime slotStart = weekStart.plusDays(dayOffset)
                            .withHour(hour)
                            .withMinute(min)
                            .truncatedTo(ChronoUnit.MINUTES);
                    ZonedDateTime slotEnd = slotStart.plusMinutes(30);
                    allSlots.add(new SimpleTutorSlot(slotStart, slotEnd, "empty"));
                }
            }
        }

        updateWeekLabel();

        ZonedDateTime weekEnd = weekStart.plusDays(6);

        //personalize slots
        tutorH.queryTutorSlots(currentTutor,  new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allTimeSlots) {
                updateSlotsWithTimeSlots(allTimeSlots, weekStart, weekEndExclusive);
            }
            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed to load slots: " + error);
            }
        });

        /*tutorH.getAllSlotsByStatus("booked", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> allTimeSlots) {
                updateSlotsWithTimeSlots(allTimeSlots, weekStart, weekEnd);
            }

            @Override
            public void onFailure(String error) {
                Log.e("TutorAvailability", "Failed to load booked slots: " + error);
            }
        });
*/
    }

    //HELPER METHOD TO PERSONALIZE THE SLOTS
    private void updateSlotsWithTimeSlots(List<TimeSlot> allTimeSlots, ZonedDateTime weekStart, ZonedDateTime weekEndExclusive) {
        int sessionCounter = 1; // start numbering from 1

        for (TimeSlot ts : allTimeSlots) {
            if (ts.getTutor() == null || ts.getTutor().getEmail() == null) continue;
            if (!ts.getTutor().getEmail().equals(currentTutor.getEmail())) continue;

            ZonedDateTime tStart = ts.getStartDate().truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime tEnd   = ts.getEndDate().truncatedTo(ChronoUnit.MINUTES);

            // EXCLUDE if the interval [tStart, tEnd) lies completely outside [weekStart, weekEndExclusive)
            if (tEnd.isBefore(weekStart) || !tStart.isBefore(weekEndExclusive)) continue;

            // Mark all overlapping SimpleTutorSlots with the SAME session number
            if ("cancelled".equals(ts.getStatus())) {
                for (SimpleTutorSlot s : allSlots) {
                    if (s.start.isBefore(tEnd) && s.end.isAfter(tStart)) {
                        s.status = "cancelled";
                        s.name = "cancelled";
                    }
                }
            } else {
                for (SimpleTutorSlot s : allSlots) {
                    if (s.start.isBefore(tEnd) && s.end.isAfter(tStart)) {
                        s.status = ts.getStatus();
                        s.name = "session " + sessionCounter; // same number for all slots in this session
                    }
                }
                sessionCounter++;
            }
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    //INITIALIZE ADANTER
    private void setupAdapter() {
        adapter = new SlotCreateTutorAdapter(allSlots, selected -> {
            selectedSlots = new ArrayList<>(selected);
            btnCreateAvailability.setVisibility(selectedSlots.isEmpty() ? View.GONE : View.VISIBLE);
        });

        //VERTICAL LAYOUT LIKE CALENDAR
        rvSlots.setLayoutManager(new GridLayoutManager(this, 7, RecyclerView.VERTICAL, false));
        rvSlots.setAdapter(adapter);
    }

    //METHOD TO CREATE NEW SESSIONS
    private void handleCreateAvailability() {
        if (selectedSlots.isEmpty()) return;

        List<SimpleTutorSlot> slotsToCreate = new ArrayList<>(selectedSlots);
        selectedSlots.clear();
        btnCreateAvailability.setVisibility(View.GONE);

        //filter already taken slots
        slotsToCreate = slotsToCreate.stream()
                .filter(s -> "empty".equals(s.status))
                .collect(Collectors.toList());

        if (slotsToCreate.isEmpty()) return;

        //group by day
        Map<DayOfWeek, List<SimpleTutorSlot>> slotsByDay = new HashMap<>();
        for (SimpleTutorSlot s : slotsToCreate) {
            slotsByDay.computeIfAbsent(s.start.getDayOfWeek(), k -> new ArrayList<>()).add(s);
        }

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

    //HELPER METHOD THAT CREATES SESSIONS
    private void createAllAvailabilitySequential(List<ZonedDateTime[]> sessions) {
        if (sessions.isEmpty()) {
            runOnUiThread(() -> generateSlotsForWeek(currentWeekStart));
            return;
        }

        ZonedDateTime[] session = sessions.remove(0);
        tutorH.createNewAvailability(currentTutor, session[0], session[1], needsApproval, new TutorCallback() {
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

    //HELPER METHOD TO CREATE SESSIONS
    private void updateLocalSlots(ZonedDateTime start, ZonedDateTime end) {
        for (SimpleTutorSlot s : allSlots) {
            if ("empty".equals(s.status) && !s.start.isBefore(start) && !s.end.isAfter(end)) {
                s.status = "open";
            }
        }
        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }
}

