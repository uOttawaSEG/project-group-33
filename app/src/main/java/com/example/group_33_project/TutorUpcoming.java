package com.example.group_33_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class TutorUpcoming extends AppCompatActivity{

    private static final String SCREEN_SETTINGS = "ScreenSettings";
    private static final String BUTTON_VISIBILITY = "ButtonVisibility";
    private TutorHandling tutorHandling;
    private Tutor currentTutor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen11_upcomingtut);
        EdgeToEdge.enable(this);

        currentTutor = (Tutor) getIntent().getSerializableExtra("tutor"); //get tutor as an object
        if (currentTutor == null) {
            Toast.makeText(this, "Error: No tutor logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Button hiddenButton = findViewById(R.id.screen11_psessionsr);

        // RETRIEVE SHAREDPREFERENCES
        SharedPreferences prefs = getSharedPreferences(SCREEN_SETTINGS, Context.MODE_PRIVATE);

        // GET THE SAVED STATE OF THE CHECKBOX
        boolean shouldShowButton = prefs.getBoolean(BUTTON_VISIBILITY, false); // THE DEFAULT IS FALSE

        // SETS THE BUTTONS VISIBILITY
        if (shouldShowButton) {
            // CHECKBOX WAS CHECKED, ENSURES BUTTON IS VISIBLE
            hiddenButton.setVisibility(View.GONE);
        } else {
            // CHECKBOX WAS UNCHECKED, ENSURES BUTTON IS HIDDEN
            hiddenButton.setVisibility(View.VISIBLE);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tutorupcoming), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FINDING THE ID OF BUTTONS
        Button Back = findViewById(R.id.screen11_back);
        Button Prequest = findViewById(R.id.screen11_psessionsr);

        //SWITCH TO PENDING SESSION REQUEST SCREEN
        Prequest.setOnClickListener(v -> {
            Intent intent = new Intent(TutorUpcoming.this, TutorPendingRequest.class);
            intent.putExtra("tutor", currentTutor);
            startActivity(intent);
//            finish();
        });



        //GOES BACK TO TUTORIN SCREEN
//        Back.setOnClickListener(v -> {
//            Intent intent = new Intent(TutorUpcoming.this, TutorIn.class);
//            startActivity(intent);
//            finish();
//        });
        Back.setOnClickListener(v -> finish()); //<----use this to go back instead

        tutorHandling = new TutorHandling();
        loadUpcomingSession(currentTutor);

    }
    //LOAD UPCOMING SESSIONS
    private void loadUpcomingSession(Tutor tutor) {

        tutorHandling.getAllSlotsByStatus("booked", new SlotListCallback() {
            @Override
            public void onSuccess(List<TimeSlot> slotList) {
                List<TimeSlot> mySlots = new ArrayList<>(); //ARRAY OF BOOKED TIME SLOTS OF TUTOR
                for (TimeSlot slot : slotList) {
                    Log.d("TutorUpcoming", "Slot tutorEmail: " + slot.getTutor().getEmail() +
                            ", Current tutor email: " + tutor.getEmail());
                    if (slot.getTutor().getEmail().equals(tutor.getEmail())) {
                        mySlots.add(slot);
                    }
                }
                if (mySlots.isEmpty()) {
                    Toast.makeText(TutorUpcoming.this, "No upcoming booked sessions", Toast.LENGTH_LONG).show();
                } else {
                    //DISPLAY THE BOOKED SESSIONS
                    Toast.makeText(TutorUpcoming.this, "Upcoming booked sessions" + mySlots.size(), Toast.LENGTH_LONG).show();


                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(TutorUpcoming.this, "Failed to load sessions: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}