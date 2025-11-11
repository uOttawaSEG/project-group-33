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
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        tutor = (Tutor) getIntent().getSerializableExtra("tutor");
        tutorHandling = new TutorHandling();

        if (tutor == null) {
            Toast.makeText(this, "Tutor object not passed correctly!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Button back = findViewById(R.id.screen13_back);
        back.setOnClickListener(v -> finish());

        // RecyclerView setup
        RecyclerView rv = findViewById(R.id.rvPastSlots);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PastTimeSlotAdapter(tutor);
        rv.setAdapter(adapter);

        loadPastSessions(tutor);
    }

    private void loadPastSessions(Tutor tutor) {
        tutorHandling.getAllSlotsByStatus("completed", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slotList) {
                pastSlots.clear();
                for (TimeSlot slot : slotList) {
                    if (slot.getTutor() != null
                            && slot.getTutor().getEmail() != null
                            && slot.getTutor().getEmail().equalsIgnoreCase(tutor.getEmail())) {
                        pastSlots.add(slot);
                    }
                }

                adapter.setData(pastSlots); // updates the list and refreshes UI

                if (pastSlots.isEmpty()) {
                    Toast.makeText(TutorPast.this, "No past sessions", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TutorPast.this, "Past sessions: " + pastSlots.size(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TutorPast.this, "Failed to load sessions: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
