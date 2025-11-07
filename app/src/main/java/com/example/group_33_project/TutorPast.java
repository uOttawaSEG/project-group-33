package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class TutorPast extends AppCompatActivity{

    private TutorHandling tutorHandling;
    private Tutor tutor;

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

        tutor = (Tutor) getIntent().getSerializableExtra("tutor"); //get tutor object
        tutorHandling = new TutorHandling();

        if (tutor == null) {
            Toast.makeText(this, "Tutor object not passed correctly!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen13_back);

        //GOES BACK TO TUTORIN SCREEN
        Back.setOnClickListener(v -> finish());

        loadPastSessions(tutor);
    }

    //LOAD THE PAST SESSIONS
    private void loadPastSessions(Tutor tutor){
        //LOAD THE PAST SESSIONS
        tutorHandling.getAllSlotsByStatus("completed", new SlotListCallback(){
            @Override
            public void onSuccess(List<TimeSlot> slotList) {
                List<TimeSlot> pastSlots = new ArrayList<>(); //ARRAY OF COMPLETED TIME SLOTS OF TUTOR
                for (TimeSlot slot : slotList) {
                    if (slot.getTutor() != null && slot.getTutor().getEmail().equals(tutor.getEmail())) {
                        pastSlots.add(slot);
                    }
                }
                if (pastSlots.isEmpty()) {
                    Toast.makeText(TutorPast.this, "No past sessions", Toast.LENGTH_LONG).show();
                } else {
                    //DISPLAY THE PAST SESSIONS
                    Toast.makeText(TutorPast.this, "Past sessions" + pastSlots.size(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TutorPast.this, "Failed to load sessions: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
