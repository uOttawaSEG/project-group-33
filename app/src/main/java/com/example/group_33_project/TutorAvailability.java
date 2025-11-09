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
import java.time.ZoneId;
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
    private final ZoneId zone = ZoneId.systemDefault();
    private SlotCreateTutorAdapter adapter;

    private Button btnNextWeek, btnPrevWeek, btnCreateAvailability, btnBack;

    private final List<SimpleTutorSlot> allSlots = new ArrayList<>();
    private final List<TimeSlot> tutorsSlots = new ArrayList<>();
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

        currentWeekStart = ZonedDateTime.now()
                .with(DayOfWeek.SUNDAY)
                .truncatedTo(ChronoUnit.DAYS);

        setupWeekNavigation();
        setupAdapter();
        generateSlotsForWeek(currentWeekStart);

        btnCreateAvailability.setOnClickListener(v -> handleCreateAvailability());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorAvailability.this, TutorIn.class);
            intent.putExtra("tutor", currentTutor);  //pass tutor object
            startActivity(intent);
            finish(); // Optional — prevents returning to this screen via back button
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

        ZonedDateTime weekEnd = weekStart.plusDays(7);

        // 1️⃣ Create all possible half-hour slots for the week
        for (int hour = 9; hour < 21; hour++) {       // outer: hours
            for (int min = 0; min < 60; min += 30) {
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) { // inner: day
                    ZonedDateTime dayStart = weekStart.plusDays(dayOffset);
                    ZonedDateTime slotStart = dayStart.withHour(hour).withMinute(min).truncatedTo(ChronoUnit.MINUTES);
                    ZonedDateTime slotEnd = slotStart.plusMinutes(30);
                    allSlots.add(new SimpleTutorSlot(slotStart, slotEnd, "empty", hour * 2 + min / 30 + 1));
                }
            }
        }


        // 2️⃣ Load confirmed availability (open + booked) for the current tutor and week
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

    // Helper to mark slots in the UI
    private void updateSlotsWithTimeSlots(List<TimeSlot> allTimeSlots, ZonedDateTime weekStart, ZonedDateTime weekEnd) {
        for (TimeSlot ts : allTimeSlots) {
            if (ts.getTutor() == null || ts.getTutor().getEmail() == null ||
                    !ts.getTutor().getEmail().equals(currentTutor.getEmail())) continue;

            ZonedDateTime tStart = ts.getStartDate().truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime tEnd = ts.getEndDate().truncatedTo(ChronoUnit.MINUTES);

            // Only include slots in current week
            if (tEnd.isBefore(weekStart) || tStart.isAfter(weekEnd)) continue;

            for (SimpleTutorSlot s : allSlots) {
                if (s.start.isBefore(tEnd) && s.end.isAfter(tStart)) {
                    s.status = ts.getStatus();
                    s.name = "session " + count;
                }
            }
            count++;
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }


    // ------------------------------------------------------------------------

    private void setupAdapter() {
        adapter = new SlotCreateTutorAdapter(allSlots, selected -> {
            selectedSlots = new ArrayList<>(selected);
            btnCreateAvailability.setVisibility(selectedSlots.isEmpty() ? View.GONE : View.VISIBLE);
        });

        rvSlots.setLayoutManager(new GridLayoutManager(this, 7, RecyclerView.VERTICAL, false));
        rvSlots.setAdapter(adapter);
    }

    // ------------------------------------------------------------------------

    private void handleCreateAvailability() {
        if (selectedSlots.isEmpty()) return;

        // 1️⃣ Copy and clear selections immediately
        List<SimpleTutorSlot> slotsToCreate = new ArrayList<>(selectedSlots);
        selectedSlots.clear();
        btnCreateAvailability.setVisibility(View.GONE);

        // 2️⃣ Filter out already existing slots
        slotsToCreate = slotsToCreate.stream()
                .filter(s -> "empty".equals(s.status))
                .collect(Collectors.toList());

        if (slotsToCreate.isEmpty()) return; // nothing new to create

        // 3️⃣ Group slots by day
        Map<DayOfWeek, List<SimpleTutorSlot>> slotsByDay = new HashMap<>();
        for (SimpleTutorSlot s : slotsToCreate) {
            slotsByDay.computeIfAbsent(s.start.getDayOfWeek(), k -> new ArrayList<>()).add(s);
        }

        List<ZonedDateTime[]> sessionsToCreate = new ArrayList<>();

        for (List<SimpleTutorSlot> daySlots : slotsByDay.values()) {
            daySlots.sort(Comparator.comparing(s -> s.start));

            ZonedDateTime rangeStart = daySlots.get(0).start.truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime prevEnd = daySlots.get(0).end.truncatedTo(ChronoUnit.MINUTES);

            for (int i = 1; i < daySlots.size(); i++) {
                SimpleTutorSlot current = daySlots.get(i);
                ZonedDateTime currentStart = current.start.truncatedTo(ChronoUnit.MINUTES);
                ZonedDateTime currentEnd = current.end.truncatedTo(ChronoUnit.MINUTES);

                if (!currentStart.equals(prevEnd)) {
                    sessionsToCreate.add(new ZonedDateTime[]{rangeStart, prevEnd});
                    rangeStart = currentStart;
                }
                prevEnd = currentEnd;
            }
            // Add the last contiguous block of the day
            sessionsToCreate.add(new ZonedDateTime[]{rangeStart, prevEnd});
        }

        // 4️⃣ Start sequential creation
        createAllAvailabilitySequential(sessionsToCreate);
    }

    private void createAllAvailabilitySequential(List<ZonedDateTime[]> sessions) {
        if (sessions.isEmpty()) {
            runOnUiThread(() -> generateSlotsForWeek(currentWeekStart)); // refresh UI once
            return;
        }

        ZonedDateTime[] session = sessions.remove(0);
        tutorH.createNewAvailability(currentTutor, session[0], session[1], false, new TutorCallback() {
            @Override
            public void onSuccess(String msg) {
                createAllAvailabilitySequential(sessions);
            }

            @Override
            public void onFailure(String error) {
                boolean slotAlreadyExists = allSlots.stream().anyMatch(s ->
                        s.start.equals(session[0]) && s.end.equals(session[1])
                );

                if (!slotAlreadyExists) {
                    runOnUiThread(() -> Toast.makeText(TutorAvailability.this,
                            "Failed: " + error, Toast.LENGTH_SHORT).show());
                }

                createAllAvailabilitySequential(sessions);
            }
        });
    }


    //PREVIOUS CODE
    /*
    TutorHandling tutorHandling;
    private Tutor tutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen10_availabilitytut);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutoravailability), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tutor = (Tutor) getIntent().getSerializableExtra("tutor"); //get tutor as an object
        tutorHandling = new TutorHandling();

        if (tutor == null) {
            Toast.makeText(this, "Tutor object not passed correctly!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen10_back);
        //Button CreateSlot = findViewById(R.id.create_slot_button); //added button to xml

//        //GOES BACK TO TUTORIN SCREEN  <---------this doesnt work for some reason idk man
//        Back.setOnClickListener(v -> {
//            Intent intent = new Intent(TutorAvailability.this, TutorIn.class);
//            startActivity(intent);
//            finish();
//        });

        Back.setOnClickListener(v -> finish()); //<----use this to go back instead

        //TESTING BACKEND. I just want to see if backend logic works these r dummy values
        ZonedDateTime startTime = ZonedDateTime.of(
                LocalDateTime.of(2025, 12, 8, 13, 0),
                ZoneId.systemDefault()
        );
        ZonedDateTime endTime = ZonedDateTime.of(
                LocalDateTime.of(2025, 12, 8, 13, 30),
                ZoneId.systemDefault()
        );

        //CREATING TIME SLOT IN DATABASE
        CreateSlot.setOnClickListener(v -> {
            tutorHandling.createNewAvailability(tutor, startTime, endTime, true, new TutorCallback() {
                @Override
                public void onSuccess(String msg) {
                    Toast.makeText(TutorAvailability.this, "Slot create at " + startTime.toString() + " to " + endTime.toString() + msg, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(String msg) {
                    Toast.makeText(TutorAvailability.this, "failed " + msg, Toast.LENGTH_LONG).show();
                }
            });
        });

    }*/
}
