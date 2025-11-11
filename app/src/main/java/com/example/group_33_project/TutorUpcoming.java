package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class TutorUpcoming extends AppCompatActivity {

    private static final String SCREEN_SETTINGS = "ScreenSettings";
    private static final String BUTTON_VISIBILITY = "ButtonVisibility";
    private TutorHandling tutorHandling;
    private Tutor currentTutor;

    private RecyclerView rvSlots;
    private TextView tvWeekLabel;
    private TutorUpcomingAdapter adapter;

    private Button btnBack, btnNextWeek, btnPrevWeek, hiddenButton;

    private ZonedDateTime currentWeekStart;
    private List<TimeSlot> mySlots;
    private List<SimpleTutorSlot> allSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //BASICS
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen11_upcomingtut);
        EdgeToEdge.enable(this);

        //INITIALIZE VIEWS
        rvSlots = findViewById(R.id.rvSlots11);
        tvWeekLabel = findViewById(R.id.tvWeekLabel11);
        btnNextWeek = findViewById(R.id.btnNextWeek11);
        btnPrevWeek = findViewById(R.id.btnPrevWeek11);
        btnBack = findViewById(R.id.screen11_back);
        hiddenButton = findViewById(R.id.screen11_psessionsr);

       //GET THE TUTOR
        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor");
        if (currentTutor == null) {
            Toast.makeText(this, "Error: No tutor logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(SCREEN_SETTINGS, Context.MODE_PRIVATE);
        boolean shouldShowButton = prefs.getBoolean(BUTTON_VISIBILITY, false);
        hiddenButton.setVisibility(shouldShowButton ? View.GONE : View.VISIBLE);

        //INITIALIZE VARIABLES
        mySlots = new ArrayList<>();
        allSlots = new ArrayList<>();

        tutorHandling = new TutorHandling();
        loadUpcomingSession(currentTutor);

        //TEST
        /*
        if (mySlots.isEmpty()) {
            addTestSlots();
        }*/

        //INITIALIZE WEEK
        currentWeekStart = ZonedDateTime.now(ZoneId.of("America/Toronto"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)) // fix to ensure we start on the correct week
                .truncatedTo(ChronoUnit.DAYS);

        setupWeekNavigation();
        setupAdapter();
        generateSlotsForWeek(currentWeekStart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorupcoming), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        //BACK BUTTON
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(TutorUpcoming.this, TutorIn.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
        });

        hiddenButton.setOnClickListener(v -> {
            Intent intent = new Intent(TutorUpcoming.this, TutorPendingRequest.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
        });
    }

    //LOAD UPCOMING SESSIONS
    private void loadUpcomingSession(Tutor tutor) {
        tutorHandling.getAllSlotsByStatus("booked", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slotList) {
                runOnUiThread(() -> {
                    for (TimeSlot slot : slotList) {
                        if (slot.getTutor() != null &&
                                slot.getTutor().getEmail() != null &&
                                slot.getTutor().getEmail().equals(tutor.getEmail())) {
                            mySlots.add(slot);
                        }
                    }
                    if (mySlots.isEmpty()) {
                        Toast.makeText(TutorUpcoming.this,
                                "No upcoming booked sessions", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(TutorUpcoming.this,
                                "Upcoming booked sessions: " + mySlots.size(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(TutorUpcoming.this,
                                "Failed to load sessions: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }

    //TEST METHOD
    private void addTestSlots() {
        ZoneId zone = ZoneId.of("America/New_York");
        ZonedDateTime zdt1 = ZonedDateTime.of(2025, 11, 12, 11, 30, 0, 0, zone);
        ZonedDateTime zdt2 = ZonedDateTime.of(2025, 11, 12, 15, 30, 0, 0, zone);
        ZonedDateTime zdt3 = ZonedDateTime.of(2025, 11, 14, 11, 0, 0, 0, zone);
        ZonedDateTime zdt4 = ZonedDateTime.of(2025, 11, 14, 18, 0, 0, 0, zone);

        Student s1 = new Student("Ivanna", "Kravchenko", "i@gmail.com", "123", "1234", "math");
        Student s2 = new Student("Vincent", "Black", "i@gmail.com", "123", "1234", "math");

        mySlots.add(new TimeSlot(currentTutor, false, zdt1, zdt2, s1, "booked", "1234"));
        mySlots.add(new TimeSlot(currentTutor, false, zdt3, zdt4, s2, "booked", "12345"));
    }

    //SETUP WEEK
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

    //UPDATE WEEK
    private void updateWeekLabel() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        ZonedDateTime weekEnd = currentWeekStart.plusDays(6);
        if (tvWeekLabel != null) {
            tvWeekLabel.setText(fmt.format(currentWeekStart) + " - " + fmt.format(weekEnd));
        }
    }

    //GENERATE WEEK SLOTS
    private void generateSlotsForWeek(ZonedDateTime weekStart) {
        allSlots.clear();

        ZonedDateTime weekEndExclusive = weekStart.plusDays(7); // next Sunday 00:00

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

        for (TimeSlot ts : mySlots) {
            ZonedDateTime tStart = ts.getStartDate().truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime tEnd = ts.getEndDate().truncatedTo(ChronoUnit.MINUTES);

            if (tEnd.isBefore(weekStart) || tStart.isAfter(weekEndExclusive)) continue;

            for (SimpleTutorSlot s : allSlots) {
                if (s.start.isBefore(tEnd) && s.end.isAfter(tStart)) {
                    String lastInitial = "";
                    if (ts.getStudent() != null && ts.getStudent().getLastName() != null
                            && ts.getStudent().getLastName().length() > 0) {
                        lastInitial = ts.getStudent().getLastName().substring(0, 1);
                    }
                    s.status = ts.getStatus();
                    s.name = ts.getStudent() != null
                            ? ts.getStudent().getFirstName() + " " + lastInitial
                            : "Booked";
                }
            }
        }

        if (adapter != null) {
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }

        updateWeekLabel();
    }

    //SETUP ADAPTER
    private void setupAdapter() {
        adapter = new TutorUpcomingAdapter(allSlots);
        rvSlots.setLayoutManager(new GridLayoutManager(this, 7, RecyclerView.VERTICAL, false));
        rvSlots.setAdapter(adapter);
    }
}
