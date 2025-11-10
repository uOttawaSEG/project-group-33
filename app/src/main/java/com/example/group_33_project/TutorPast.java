package com.example.group_33_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TutorPast extends AppCompatActivity {

    private TutorHandling tutorHandling;
    private Tutor tutor;

    private final List<TimeSlot> pastSlots = new ArrayList<>();
    private PastTimeSlotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen13_pasttut);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorpast), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI
        Button back = findViewById(R.id.screen13_back);
        RecyclerView recyclerView = findViewById(R.id.pastSessionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PastTimeSlotAdapter(pastSlots);
        recyclerView.setAdapter(adapter);

        // Data
        tutor = (Tutor) getIntent().getSerializableExtra("tutor");
        tutorHandling = new TutorHandling();

        if (tutor == null) {
            Toast.makeText(this, "Tutor object not passed correctly!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        back.setOnClickListener(v -> finish());

        // Load data like AdminRequest does for accounts
        loadPastSessions(tutor);
    }

    private void loadPastSessions(Tutor t) {
        tutorHandling.getAllSlotsByStatus("completed", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slotList) {
                pastSlots.clear();
                // filter to this tutor, using equals() or getId()
                for (TimeSlot slot : slotList) {
                    if (slot.getTutor() != null) {
                        // 1) equals(): if Tutor overrides equals to compare identity (email is fine)
                        boolean sameTutor = false;
                        try {
                            sameTutor = slot.getTutor().equals(t);
                        } catch (Exception ignored) {}

                        // 2) getId() fallback if equals() isn’t reliable
                        if (!sameTutor) {
                            String thisId = tryGetTutorId(slot.getTutor());
                            String thatId = tryGetTutorId(t);
                            if (thisId != null && thisId.equals(thatId)) sameTutor = true;
                        }

                        // 3) email fallback (works with your current code)
                        if (!sameTutor && slot.getTutor().getEmail() != null && t.getEmail() != null) {
                            sameTutor = slot.getTutor().getEmail().equalsIgnoreCase(t.getEmail());
                        }

                        if (sameTutor) {
                            pastSlots.add(slot);
                        }
                    }
                }

                if (pastSlots.isEmpty()) {
                    Toast.makeText(TutorPast.this, "No past sessions", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TutorPast.this, "Past sessions: " + pastSlots.size(), Toast.LENGTH_LONG).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TutorPast.this, "Failed to load sessions: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String tryGetTutorId(Tutor tut) {
        if (tut == null) return null;
        try {
            Object id = tut.getClass().getMethod("getId").invoke(tut);
            return id == null ? null : String.valueOf(id);
        } catch (Exception e) {
            return null;
        }
    }
}
