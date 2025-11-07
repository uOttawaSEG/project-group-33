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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class TutorAvailability extends AppCompatActivity{
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
        Button CreateSlot = findViewById(R.id.create_slot_button); //added button to xml

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

    }
}
